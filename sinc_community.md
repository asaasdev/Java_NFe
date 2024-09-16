# Sincronização dos projetos Java_nfe e Java_certificado

Para envio das NFe/NFCe utilizamos na camada de comunição o projeto mantido pela comunidade chamado Java_nfe. 

A comunidade java_nfe tambem mantém o projeto Java_certificado, que é consumido pelo java_nfe (ver arquivo pom.xml). 
Para atualizar a versão dos certificados, basta sincronizar o java_nfe com a versão da comunidade, conforme passos 
descritos a seguir.

### Como sincronizar os projetos com a comunidade:

- Selecionar o branch master e fazer o sinc fork com o projeto da comunidade java_nfe ou java_certificado

![image](https://github.com/asaasdev/Java_NFe/assets/81778352/90ffb830-2fda-4201-8845-f08351f16495)

- Criar um branch de desenvolvimento à partir do branch release.

- Baixar o branch na maquina de desenvolvimento e fazer o merge com a branch master(comunidade)

- Fazer os ajustes necessários para implementar as novas regras e verificar se as novas implementações da comunidade não quebraram algum método customizado para emissão com certificado A3.

- Ajustar a versão no pom-base.xml deixando sempre igual ao pom.xml que é gerenciado pela comunidade. **Verifique sempre a versão da dependếncia java_certificado**, ela deve sempre ser a mesma do pom.xml para que a emissão de nota funcione adequadamente. Veja o problema [Erro "Connection or outbound has closed" ao transmitir NFe](https://asaasdev.atlassian.net/wiki/spaces/PROD/pages/3605561606/Erro+Connection+or+outbound+has+closed+ao+transmitir+NFe) para entender os impactos de quando esta Dependência fica desatualizada.

![image](https://github.com/asaasdev/Java_NFe/assets/81778352/1b63365c-0dda-4a31-8a88-359030036bfc)

- Verificar se alguma alteração no pom.xml que precise ser mergedada ao pom-base.xml(inserção/Alterção das dependencias)
  
- Abrir um Pull Request para o branch release
  
![image](https://github.com/asaasdev/Java_NFe/assets/81778352/e2a55a57-0afe-473a-b055-7848d2027b3f)

- Após a aprovação/merge do branch de desenvolvimento deve-se fazer o merge da branch "relase" para "master_base"

### Atualização dos arquivos xsd

Se a nova versão possuir mudanças nos arquivos .xsd, é necessário também atualizar estes arquivos no bucket 
bucket s3:baseerp/producao/xsd. Os arquivos xsd ficam no repositório Java_NFE no diretório /schemas.

1. Faça uma cópia do diretório atual como backup, e a nomeie indicando o mês/dia em que está sendo feita a 
   atualização. Isto nos permite efetuar um rollback, se necessário.
2. Carregue os novos arquivos no bucket s3:baseerp/producao/xsd.

### Build dos projetos no Jenkins:

Os projetos java_nfe e java_certificado são deployados no nexus e depois referencia no pom do projeto ex. java.api e Jsf
Foram criados verões do pom de acordo com o abiente para podemos testar:

local, sandbox, staging e prod

![image](https://github.com/asaasdev/Java_NFe/assets/81778352/250a81ef-b9ba-4e56-a14e-50c32c75119a)


Após feito o merge da branch "release" para o "master_base", precisamos fazer o build no jenkins do java_nfe/java_certificado antes do deploy do serviços que utilizam esses jar.


![image](https://github.com/asaasdev/Java_NFe/assets/81778352/8c0d3eee-efeb-41d3-954f-b5de3343936d)


### Atualização do pacote nos serviços

Deve-se atualizar para a nova versão do java-nfe nos repositórios erp-services e erp-jsf. Os arquivos a alterar são os seguintes:
- [pom.xml](https://github.com/asaasdev/erp-jsf/blob/master/libs/baseerp.nfe.integracao/pom.xml) do nfe.integracao no repositório erp-jsf ([ver PR exemplo](https://github.com/asaasdev/erp-jsf/pull/1744/files))
- [pom.xml](https://github.com/asaasdev/erp-services/blob/master/libs/nfe.integracao/pom.xml#L49) do nfe.integracao no repositório erp-services ([ver PR exemplo](https://github.com/asaasdev/erp-services/pull/2779/files))

Após atualização executar o release dos serviços normalmente.
