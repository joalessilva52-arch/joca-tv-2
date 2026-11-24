# JOCA TV 2.0 — Projeto Android TV

## Incluído nesta versão
- Interface escura azul/preto, navegação por botões e D-pad.
- Importação de playlist M3U por URL.
- Login básico Xtream Codes API via HTTPS.
- Leitura de categorias e canais ao vivo Xtream.
- Player integrado Media3/ExoPlayer.
- Favoritos locais.
- Páginas iniciais de TV ao Vivo, Filmes, Séries e Favoritos.

## Limitações honestas da versão 2.0
- O endpoint de filmes e séries ainda não está ligado à interface; as páginas estão preparadas para a próxima etapa.
- Importação M3U é por URL nesta versão; seletor de arquivo local fica para atualização posterior.
- O Xtream nesta versão carrega canais ao vivo. VOD e séries precisam de implementação adicional.
- A compatibilidade varia conforme o formato de transmissão do provedor. O player pode não reproduzir alguns codecs/formatos.
- A conexão Xtream exige HTTPS nesta versão. Não há suporte a servidores HTTP sem criptografia.
- As credenciais Xtream ficam apenas na memória durante a sessão; não são persistidas.
- Não inclui listas, credenciais, canais, filmes ou séries. Use apenas serviços e links autorizados.

## Compilar pelo celular com Code on the Go
1. Instale o Code on the Go apenas pelo site oficial: https://appdevforall.org/code-on-the-go/
2. Na primeira execução, siga a configuração dos componentes de desenvolvimento. A configuração pode baixar vários GB.
3. Extraia `JOCA-TV-2.0-Projeto.zip` em uma pasta local.
4. No Code on the Go, use **Open existing project** e selecione a pasta `JOCA-TV-2.0`.
5. Aguarde o Gradle sincronizar e baixar as dependências.
6. Use Build/Run para compilar. Exporte o APK gerado pelo recurso Export do Code on the Go.

## Requisitos publicados do Code on the Go
A documentação oficial informa Android 9 ou superior, CPU ARM 32-bit ou 64-bit, cerca de 3,5 GB livres (mais espaço pode ser necessário durante builds) e recomenda 4 GB de RAM livre para uma experiência melhor. Confira a compatibilidade do seu aparelho antes de instalar.

## Instalar na TCL Google TV
Transfira o APK para a TV por um método confiável e instale permitindo fontes desconhecidas apenas para o app usado na transferência/gerenciador. Os menus variam por versão do Google TV.

## Identidade
Nome: JOCA TV 2.0
Pacote: br.com.jocatv
Versão: 2.0 (versionCode 2)
