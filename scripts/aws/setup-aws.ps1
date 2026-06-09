$ErrorActionPreference = 'Stop'

$Region   = 'us-east-1'
$Suffix   = 'tary'
$SesEmail = 'tary.junior47@gmail.com'

$Bucket  = "vitalink-documents-$Suffix"
$Topic   = 'vitalink-appointments'
$Queue   = 'vitalink-appointments-queue'
$SsmPath = '/vitalink/prod'
$IamUser = 'vitalink-app'

function Step($msg) { Write-Host "`n=== $msg ===" -ForegroundColor Cyan }

aws sts get-caller-identity --output text | Out-Null
$Account = (aws sts get-caller-identity --query Account --output text)
Write-Host "Conta AWS: $Account | Regiao: $Region"

Step "S3: bucket $Bucket"
$exists = aws s3api head-bucket --bucket $Bucket 2>$null; $ok = $?
if (-not $ok) {
    if ($Region -eq 'us-east-1') {
        aws s3api create-bucket --bucket $Bucket --region $Region | Out-Null
    } else {
        aws s3api create-bucket --bucket $Bucket --region $Region `
            --create-bucket-configuration "LocationConstraint=$Region" | Out-Null
    }
}
aws s3api put-public-access-block --bucket $Bucket --public-access-block-configuration `
    "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true" | Out-Null
Write-Host "Bucket pronto."

Step "SES: identidade $SesEmail"
aws sesv2 create-email-identity --email-identity $SesEmail --region $Region 2>$null | Out-Null
Write-Host "Identidade criada. VERIFIQUE seu e-mail e clique no link de confirmacao."
Write-Host "Em sandbox o SES so envia para e-mails verificados (verifique tambem o destinatario de teste)."

Step "SNS: topico $Topic"
$TopicArn = (aws sns create-topic --name $Topic --region $Region --query TopicArn --output text)
Write-Host "TopicArn: $TopicArn"

Step "SQS: fila $Queue"
$QueueUrl = (aws sqs create-queue --queue-name $Queue --region $Region --query QueueUrl --output text)
$QueueArn = (aws sqs get-queue-attributes --queue-url $QueueUrl --attribute-names QueueArn `
    --region $Region --query "Attributes.QueueArn" --output text)
Write-Host "QueueUrl: $QueueUrl"
Write-Host "QueueArn: $QueueArn"

$policyObj = @{
    Version = "2012-10-17"
    Statement = @(@{
        Effect    = "Allow"
        Principal = @{ Service = "sns.amazonaws.com" }
        Action    = "sqs:SendMessage"
        Resource  = $QueueArn
        Condition = @{ ArnEquals = @{ "aws:SourceArn" = $TopicArn } }
    })
}
$policyJson = ($policyObj | ConvertTo-Json -Depth 10 -Compress)
$attrs = @{ Policy = $policyJson } | ConvertTo-Json -Depth 10 -Compress
$attrsFile = New-TemporaryFile
Set-Content -Path $attrsFile -Value $attrs -Encoding utf8
aws sqs set-queue-attributes --queue-url $QueueUrl --attributes "file://$attrsFile" --region $Region | Out-Null
Remove-Item $attrsFile -Force

$SubArn = (aws sns subscribe --topic-arn $TopicArn --protocol sqs `
    --notification-endpoint $QueueArn --region $Region --query SubscriptionArn --output text)
aws sns set-subscription-attributes --subscription-arn $SubArn `
    --attribute-name RawMessageDelivery --attribute-value true --region $Region | Out-Null
Write-Host "Fila inscrita no topico (fan-out ativo)."

Step "SSM Parameter Store: segredos em $SsmPath"
$bytes = New-Object byte[] 64
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$JwtSecret = [Convert]::ToBase64String($bytes)
aws ssm put-parameter --name "$SsmPath/APP_JWT_SECRET" --type SecureString `
    --value $JwtSecret --overwrite --region $Region | Out-Null
Write-Host "Parametro $SsmPath/APP_JWT_SECRET criado (SecureString)."

Step "IAM: usuario $IamUser"
aws iam create-user --user-name $IamUser 2>$null | Out-Null
$iamPolicy = @{
    Version = "2012-10-17"
    Statement = @(
        @{ Sid="S3";  Effect="Allow"; Action=@("s3:PutObject","s3:GetObject","s3:DeleteObject"); Resource="arn:aws:s3:::$Bucket/*" },
        @{ Sid="SES"; Effect="Allow"; Action=@("ses:SendEmail"); Resource="*" },
        @{ Sid="SNS"; Effect="Allow"; Action=@("sns:Publish"); Resource=$TopicArn },
        @{ Sid="SQS"; Effect="Allow"; Action=@("sqs:ReceiveMessage","sqs:DeleteMessage","sqs:GetQueueAttributes"); Resource=$QueueArn },
        @{ Sid="SSM"; Effect="Allow"; Action=@("ssm:GetParametersByPath","ssm:GetParameter","ssm:GetParameters"); Resource="arn:aws:ssm:$Region`:$Account`:parameter$SsmPath/*" }
    )
}
$iamFile = New-TemporaryFile
Set-Content -Path $iamFile -Value ($iamPolicy | ConvertTo-Json -Depth 10) -Encoding utf8
aws iam put-user-policy --user-name $IamUser --policy-name vitalink-app-policy `
    --policy-document "file://$iamFile" | Out-Null
Remove-Item $iamFile -Force

Step "IAM: chave de acesso"
$key = (aws iam create-access-key --user-name $IamUser --output json | ConvertFrom-Json)
$AccessKey = $key.AccessKey.AccessKeyId
$SecretKey = $key.AccessKey.SecretAccessKey

Step "PRONTO - copie para o seu .env"
@"
APP_AWS_ENABLED=true
APP_AWS_REGION=$Region
APP_AWS_ACCESS_KEY=$AccessKey
APP_AWS_SECRET_KEY=$SecretKey
APP_AWS_S3_BUCKET=$Bucket
APP_AWS_S3_PRESIGN_MINUTES=15
APP_AWS_SES_FROM=$SesEmail
APP_AWS_SNS_APPOINTMENT_TOPIC_ARN=$TopicArn
APP_AWS_SQS_CONSUMER_ENABLED=true
APP_AWS_SQS_APPOINTMENT_QUEUE_URL=$QueueUrl
APP_AWS_SSM_ENABLED=false
APP_AWS_SSM_PARAMETER_PATH=$SsmPath
"@ | Write-Host -ForegroundColor Green

Write-Host "`nGuarde a SECRET KEY agora - ela nao pode ser exibida de novo." -ForegroundColor Yellow
Write-Host "Ao terminar os testes, rode: .\scripts\aws\teardown-aws.ps1" -ForegroundColor Yellow
