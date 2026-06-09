<#
.SYNOPSIS
  Apaga TODOS os recursos AWS criados pelo setup-aws.ps1 (S3, SES, SNS, SQS, SSM, IAM).
  Use ao terminar os testes para garantir custo zero.

.DESCRIPTION
  Use as MESMAS variaveis do setup-aws.ps1 (Region/Suffix/SesEmail).
  O script tolera recursos ja inexistentes.
#>

$ErrorActionPreference = 'Continue'   # segue mesmo se algum recurso ja nao existir

# ============================ AJUSTE AQUI (igual ao setup) ============================
$Region   = 'us-east-1'
$Suffix   = 'seunome'
$SesEmail = 'troque@seu-email.com'
# =====================================================================================

$Bucket  = "vitalink-documents-$Suffix"
$Topic   = 'vitalink-appointments'
$Queue   = 'vitalink-appointments-queue'
$SsmPath = '/vitalink/prod'
$IamUser = 'vitalink-app'
$Account = (aws sts get-caller-identity --query Account --output text)

function Step($msg) { Write-Host "`n=== $msg ===" -ForegroundColor Cyan }

Step "S3: esvazia e apaga $Bucket"
aws s3 rm "s3://$Bucket" --recursive 2>$null
aws s3api delete-bucket --bucket $Bucket --region $Region 2>$null

Step "SQS: apaga fila $Queue"
$QueueUrl = (aws sqs get-queue-url --queue-name $Queue --region $Region --query QueueUrl --output text 2>$null)
if ($QueueUrl -and $QueueUrl -ne "None") { aws sqs delete-queue --queue-url $QueueUrl --region $Region 2>$null }

Step "SNS: apaga assinaturas e topico $Topic"
$TopicArn = "arn:aws:sns:$Region`:$Account`:$Topic"
$subs = (aws sns list-subscriptions-by-topic --topic-arn $TopicArn --region $Region `
    --query "Subscriptions[].SubscriptionArn" --output text 2>$null)
if ($subs) {
    foreach ($s in ($subs -split "\s+")) {
        if ($s -like "arn:*") { aws sns unsubscribe --subscription-arn $s --region $Region 2>$null }
    }
}
aws sns delete-topic --topic-arn $TopicArn --region $Region 2>$null

Step "SSM: apaga parametros em $SsmPath"
$params = (aws ssm get-parameters-by-path --path $SsmPath --recursive --region $Region `
    --query "Parameters[].Name" --output text 2>$null)
if ($params) {
    foreach ($p in ($params -split "\s+")) {
        if ($p) { aws ssm delete-parameter --name $p --region $Region 2>$null }
    }
}

Step "IAM: remove chaves, politica e usuario $IamUser"
$keys = (aws iam list-access-keys --user-name $IamUser --query "AccessKeyMetadata[].AccessKeyId" --output text 2>$null)
if ($keys) {
    foreach ($k in ($keys -split "\s+")) {
        if ($k) { aws iam delete-access-key --user-name $IamUser --access-key-id $k 2>$null }
    }
}
aws iam delete-user-policy --user-name $IamUser --policy-name vitalink-app-policy 2>$null
aws iam delete-user --user-name $IamUser 2>$null

Step "SES: remove identidade $SesEmail"
aws sesv2 delete-email-identity --email-identity $SesEmail --region $Region 2>$null

Write-Host "`nTeardown concluido. Recursos cobraveis removidos." -ForegroundColor Green
