#!/usr/bin/env bash
set -uo pipefail

REGION='us-east-1'
SUFFIX='tary'
SES_EMAIL='tary.junior47@gmail.com'

BUCKET="vitalink-documents-${SUFFIX}"
TOPIC='vitalink-appointments'
QUEUE='vitalink-appointments-queue'
SSM_PATH='/vitalink/prod'
IAM_USER='vitalink-app'

step() { echo -e "\n=== $1 ==="; }

ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
echo "Conta AWS: $ACCOUNT | Regiao: $REGION"

step "S3: bucket $BUCKET"
if ! aws s3api head-bucket --bucket "$BUCKET" 2>/dev/null; then
  if [ "$REGION" = "us-east-1" ]; then
    aws s3api create-bucket --bucket "$BUCKET" --region "$REGION" >/dev/null
  else
    aws s3api create-bucket --bucket "$BUCKET" --region "$REGION" \
      --create-bucket-configuration "LocationConstraint=$REGION" >/dev/null
  fi
fi
aws s3api put-public-access-block --bucket "$BUCKET" --public-access-block-configuration \
  "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true" >/dev/null
echo "Bucket pronto."

step "SES: identidade $SES_EMAIL"
aws sesv2 create-email-identity --email-identity "$SES_EMAIL" --region "$REGION" >/dev/null 2>&1 || true
echo "Identidade criada. VERIFIQUE seu e-mail e clique no link de confirmacao."
echo "Em sandbox o SES so envia para e-mails verificados (verifique tambem o destinatario de teste)."

step "SNS: topico $TOPIC"
TOPIC_ARN=$(aws sns create-topic --name "$TOPIC" --region "$REGION" --query TopicArn --output text)
echo "TopicArn: $TOPIC_ARN"

step "SQS: fila $QUEUE"
QUEUE_URL=$(aws sqs create-queue --queue-name "$QUEUE" --region "$REGION" --query QueueUrl --output text)
QUEUE_ARN=$(aws sqs get-queue-attributes --queue-url "$QUEUE_URL" --attribute-names QueueArn \
  --region "$REGION" --query "Attributes.QueueArn" --output text)
echo "QueueUrl: $QUEUE_URL"
echo "QueueArn: $QUEUE_ARN"

jq -n --arg arn "$QUEUE_ARN" --arg topic "$TOPIC_ARN" \
  '{Policy: ({Version:"2012-10-17",Statement:[{Effect:"Allow",Principal:{Service:"sns.amazonaws.com"},Action:"sqs:SendMessage",Resource:$arn,Condition:{ArnEquals:{"aws:SourceArn":$topic}}}]} | tostring)}' \
  > /tmp/vitalink-sqs-attrs.json
aws sqs set-queue-attributes --queue-url "$QUEUE_URL" \
  --attributes file:///tmp/vitalink-sqs-attrs.json --region "$REGION"

SUB_ARN=$(aws sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs \
  --notification-endpoint "$QUEUE_ARN" --region "$REGION" --query SubscriptionArn --output text)
aws sns set-subscription-attributes --subscription-arn "$SUB_ARN" \
  --attribute-name RawMessageDelivery --attribute-value true --region "$REGION"
echo "Fila inscrita no topico (fan-out ativo)."

step "SSM Parameter Store: segredos em $SSM_PATH"
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
aws ssm put-parameter --name "$SSM_PATH/APP_JWT_SECRET" --type SecureString \
  --value "$JWT_SECRET" --overwrite --region "$REGION" >/dev/null
echo "Parametro $SSM_PATH/APP_JWT_SECRET criado (SecureString)."

step "IAM: usuario $IAM_USER"
aws iam create-user --user-name "$IAM_USER" >/dev/null 2>&1 || true
cat > /tmp/vitalink-iam.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    { "Sid":"S3",  "Effect":"Allow", "Action":["s3:PutObject","s3:GetObject","s3:DeleteObject"], "Resource":"arn:aws:s3:::$BUCKET/*" },
    { "Sid":"SES", "Effect":"Allow", "Action":["ses:SendEmail"], "Resource":"*" },
    { "Sid":"SNS", "Effect":"Allow", "Action":["sns:Publish"], "Resource":"$TOPIC_ARN" },
    { "Sid":"SQS", "Effect":"Allow", "Action":["sqs:ReceiveMessage","sqs:DeleteMessage","sqs:GetQueueAttributes"], "Resource":"$QUEUE_ARN" },
    { "Sid":"SSM", "Effect":"Allow", "Action":["ssm:GetParametersByPath","ssm:GetParameter","ssm:GetParameters"], "Resource":"arn:aws:ssm:$REGION:$ACCOUNT:parameter$SSM_PATH/*" }
  ]
}
EOF
aws iam put-user-policy --user-name "$IAM_USER" --policy-name vitalink-app-policy \
  --policy-document file:///tmp/vitalink-iam.json

step "IAM: chave de acesso"
KEY_JSON=$(aws iam create-access-key --user-name "$IAM_USER" --output json)
ACCESS_KEY=$(echo "$KEY_JSON" | jq -r '.AccessKey.AccessKeyId')
SECRET_KEY=$(echo "$KEY_JSON" | jq -r '.AccessKey.SecretAccessKey')

step "PRONTO - copie para o seu .env"
cat <<EOF
APP_AWS_ENABLED=true
APP_AWS_REGION=$REGION
APP_AWS_ACCESS_KEY=$ACCESS_KEY
APP_AWS_SECRET_KEY=$SECRET_KEY
APP_AWS_S3_BUCKET=$BUCKET
APP_AWS_S3_PRESIGN_MINUTES=15
APP_AWS_SES_FROM=$SES_EMAIL
APP_AWS_SNS_APPOINTMENT_TOPIC_ARN=$TOPIC_ARN
APP_AWS_SQS_CONSUMER_ENABLED=true
APP_AWS_SQS_APPOINTMENT_QUEUE_URL=$QUEUE_URL
APP_AWS_SSM_ENABLED=false
APP_AWS_SSM_PARAMETER_PATH=$SSM_PATH
EOF

echo ""
echo ">> Guarde a SECRET KEY agora - ela nao pode ser exibida de novo."
echo ">> Ao terminar os testes, rode: bash teardown-aws.sh"
rm -f /tmp/vitalink-sqs-attrs.json /tmp/vitalink-iam.json
