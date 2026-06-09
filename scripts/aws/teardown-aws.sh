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
ACCOUNT=$(aws sts get-caller-identity --query Account --output text)

step() { echo -e "\n=== $1 ==="; }

step "S3: esvazia e apaga $BUCKET"
aws s3 rm "s3://$BUCKET" --recursive 2>/dev/null || true
aws s3api delete-bucket --bucket "$BUCKET" --region "$REGION" 2>/dev/null || true

step "SQS: apaga fila $QUEUE"
QUEUE_URL=$(aws sqs get-queue-url --queue-name "$QUEUE" --region "$REGION" --query QueueUrl --output text 2>/dev/null || true)
[ -n "${QUEUE_URL:-}" ] && [ "$QUEUE_URL" != "None" ] && aws sqs delete-queue --queue-url "$QUEUE_URL" --region "$REGION" 2>/dev/null || true

step "SNS: apaga assinaturas e topico $TOPIC"
TOPIC_ARN="arn:aws:sns:${REGION}:${ACCOUNT}:${TOPIC}"
for s in $(aws sns list-subscriptions-by-topic --topic-arn "$TOPIC_ARN" --region "$REGION" \
            --query "Subscriptions[].SubscriptionArn" --output text 2>/dev/null); do
  [[ "$s" == arn:* ]] && aws sns unsubscribe --subscription-arn "$s" --region "$REGION" 2>/dev/null || true
done
aws sns delete-topic --topic-arn "$TOPIC_ARN" --region "$REGION" 2>/dev/null || true

step "SSM: apaga parametros em $SSM_PATH"
for p in $(aws ssm get-parameters-by-path --path "$SSM_PATH" --recursive --region "$REGION" \
            --query "Parameters[].Name" --output text 2>/dev/null); do
  [ -n "$p" ] && aws ssm delete-parameter --name "$p" --region "$REGION" 2>/dev/null || true
done

step "IAM: remove chaves, politica e usuario $IAM_USER"
for k in $(aws iam list-access-keys --user-name "$IAM_USER" \
            --query "AccessKeyMetadata[].AccessKeyId" --output text 2>/dev/null); do
  [ -n "$k" ] && aws iam delete-access-key --user-name "$IAM_USER" --access-key-id "$k" 2>/dev/null || true
done
aws iam delete-user-policy --user-name "$IAM_USER" --policy-name vitalink-app-policy 2>/dev/null || true
aws iam delete-user --user-name "$IAM_USER" 2>/dev/null || true

step "SES: remove identidade $SES_EMAIL"
aws sesv2 delete-email-identity --email-identity "$SES_EMAIL" --region "$REGION" 2>/dev/null || true

echo -e "\nTeardown concluido. Recursos cobraveis removidos."
