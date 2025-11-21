package com.infnet.artigoEventos.controller.evento;

/**
 * Constantes reutilizáveis para testes do EventoController.
 * Centraliza IDs, emails, mensagens de erro e outros valores comuns.
 */
public final class EventoControllerTestConstants {

  // ==================================================================================
  // IDs - Identificadores para testes
  // ==================================================================================

  public static final Integer EVENTO_ID_EXISTENTE = 1;
  public static final Integer EVENTO_ID_INEXISTENTE = 99;
  public static final Integer EVENTO_ID_NOVO = 10;
  public static final Integer PARTICIPANTE_ID_EXISTENTE = 20;
  public static final Integer PARTICIPANTE_ID_INEXISTENTE = 200;
  public static final Integer PARTICIPANTE_ID_NOVO = 1;

  // ==================================================================================
  // EMAILS - Endereços de email para testes
  // ==================================================================================

  public static final String EMAIL_ORGANIZADOR = "organizador@test.com";
  public static final String EMAIL_USUARIO_TESTE = "email@teste.com";
  public static final String EMAIL_INEXISTENTE = "naoexiste@x.com";

  // ==================================================================================
  // DADOS DE TESTE - Valores padrão para entidades
  // ==================================================================================

  public static final String EVENTO_NOME_TESTE = "Evento Teste";
  public static final String EVENTO_LOCAL_SP = "SP";
  public static final String EVENTO_LOCAL_RJ = "RJ";
  public static final String EVENTO_DESCRICAO = "Descrição de teste";
  public static final String PARTICIPANTE_NOME = "João";
  public static final String PARTICIPANTE_EMAIL = "joao@test.com";

  // ==================================================================================
  // MENSAGENS DE ERRO - Mensagens esperadas em cenários de erro
  // ==================================================================================

  public static final String MSG_EVENTO_NAO_ENCONTRADO = "Evento não encontrado";
  public static final String MSG_PARTICIPANTE_NAO_ENCONTRADO = "Participante não encontrado";
  public static final String MSG_USUARIO_ORGANIZADOR_NAO_ENCONTRADO = "Usuario Organizador nao encotrado";
  public static final String MSG_PARTICIPANTE_JA_INSCRITO = "Participante já inscrito";
  public static final String MSG_PARTICIPANTE_NAO_PERTENCE_EVENTO = "Participante não pertence ao evento";
  public static final String MSG_ERRO_PROCESSANDO_IMAGEM = "Erro processando a imagem";

  // ==================================================================================
  // ARQUIVOS - Informações sobre arquivos de teste
  // ==================================================================================

  public static final String IMAGEM_FILENAME = "foto.png";
  public static final String IMAGEM_CONTENT_TYPE = "image/png";
  public static final String IMAGEM_FAKE_CONTENT = "fake-image";

  /**
   * Construtor privado para prevenir instanciação.
   */
  private EventoControllerTestConstants() {
    throw new UnsupportedOperationException("Classe de constantes não pode ser instanciada");
  }
}
