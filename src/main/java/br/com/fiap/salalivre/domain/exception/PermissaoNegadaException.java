package br.com.fiap.salalivre.domain.exception;

public class PermissaoNegadaException extends RegraDeNegocioException {
    public PermissaoNegadaException(String mensagem) {
        super(mensagem);
    }
}
