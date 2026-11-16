# Versículo Diário

Aplicativo Android nativo para compor uma imagem com um versículo e aplicá-la somente à tela de bloqueio.

## APK de teste 0.1.0

O workflow **Build Android APK** já compilou com sucesso o APK de depuração. Para baixar:

1. Abra https://github.com/gustavoeliezer04-oss/Versiculo-diario/actions
2. Abra a execução mais recente com conclusão verde.
3. Na seção **Artifacts**, baixe `versiculo-diario-debug`.
4. Extraia o ZIP e instale `app-debug.apk no Android.

O artefato do GitHub Actions expira após o período indicado na própria página da execução.

## O que a versão 0.1.0 faz

- Selecionar uma foto pela galeria.
- Pré-visualizar o texto sobre a foto.
- Alternar entre frases demonstrativas.
- Aplicar o resultado somente à tela de bloqueio por `WallpaperManager.FLAG_LOCK`.
- Agendar uma atualização periódica com WorkManager.
- Não publicar notificações.
- Evitar substituir uma imagem de bloqueio alterada manualmente fora do aplicativo, comparando o identificador do papel de parede.

## Limitações conhecidas

- A biblioteca atual contém sete paráfrases demonstrativas, não traduções bíblicas completas.
- A atualização do WorkManager é periódica e pode atrasar devido às políticas de bateria do Android/MIUI; não é um alarme exato à meia-noite.
- O app ainda não consegue importar automaticamente uma nova foto definida diretamente nas configurações do aparelho.
- As opções de fontes, cores e posicionamento ainda são básicas e serão ampliadas.

## Traduções e fontes

Consulte [a política de licenças bíblicas](docs/LICENCAS_BIBLICAS.md) e [as famílias de fonte](docs/ESTILOS_DE_FONTE.md). Traduções protegidas por direitos autorais não serão redistribuídas sem autorização.

## Compilação

O GitHub Actions compila com JDK 17, Gradle 8.9 e Android Gradle Plugin. O workflow pode ser iniciado manualmente em Actions > Build Android APK > Run workflow.
