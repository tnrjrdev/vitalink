# Imagens de evidência (prints)

Coloque aqui os prints que comprovam as integrações AWS funcionando. Use
**exatamente** estes nomes de arquivo (a seção "Demonstração das integrações AWS"
do README principal aponta para eles):

| Arquivo | O que o print deve mostrar | Origem |
|---|---|---|
| `swagger-documentos.png` | Tag "Documentos" no Swagger com os endpoints de upload/download | http://localhost:8080/swagger-ui.html |
| `ses-email.png` | E-mail de confirmação recebido na caixa de entrada | Gmail/cliente de e-mail |
| `ses-verified.png` | Identidade verificada (status "Verificado") | Console AWS -> SES -> Identidades |
| `s3-bucket.png` | Bucket `vitalink-documents-*` (ou o objeto enviado dentro dele) | Console AWS -> S3 |
| `sns-topic.png` | Tópico `vitalink-appointments` | Console AWS -> SNS -> Tópicos |
| `sqs-queue.png` | Fila `vitalink-appointments-queue` (inscrita no tópico) | Console AWS -> SQS -> Filas |
| `iam-user.png` | Usuário `vitalink-app` (least-privilege) | Console AWS -> IAM -> Usuários |

## Antes de commitar — checklist de seguranca

- [ ] Nenhuma **Secret Key** visível.
- [ ] Nenhum **Access Key ID** (`AKIA...`) visível — atencao a URLs pre-assinadas (`downloadUrl`).
- [ ] **Account ID** borrado (canto superior direito e dentro de ARNs).
- [ ] Sem CPFs/dados pessoais reais (use os ficticios dos exemplos).

> Dica: PNG fica nitido para telas. Mantenha os arquivos abaixo de ~500 KB
> (recorte só a area relevante) para o repositorio nao inchar.
