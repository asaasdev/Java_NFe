# Sincronização dos projetos Java_nfe e Java_certificado

Para envio das NFe/NFCe utilizamos na camada de comunição o projeto mantido pela comunidade chamado Java_nfe. 

A comunidade java_nfe tambem mantem o projeto Java_certificado.

### Como sincronizar os projetos com a comunidade:

- Selecionar o branch master e fazer o sinc fork com o projeto da comunidade java_nfe ou java_certificado

![image](https://github.com/asaasdev/Java_NFe/assets/81778352/90ffb830-2fda-4201-8845-f08351f16495)

- Criar um branch de desenvolvimento à partir do branch release.

- Baixar o branch na maquina de desenvolvimento e fazer o merge com a branch master(comunidade)

- Fazer os ajustes necessários para implementar as novas regras e verificar se as novas implementações da comunidade não quebraram algum método customizado para emissão com certificado A3.

- Ajustar a versão no pom-base.xml deixando sempre igual ao pom.xml que é gerenciado pela comunidade.

![image](https://github.com/asaasdev/Java_NFe/assets/81778352/1b63365c-0dda-4a31-8a88-359030036bfc)

- Verificar se alguma alteração no pom.xml que precise ser mergedada ao pom-base.xml(inserção/Alterção das dependencias)
  
- Abrir um Pull Request para o branch release
  
![image](https://github.com/asaasdev/Java_NFe/assets/81778352/e2a55a57-0afe-473a-b055-7848d2027b3f)

- Após a aprovação/merge do branch de desenvolvimento deve-se fazer o merge da branc "relase" para "master_base"




