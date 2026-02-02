package br.com.fiap.salalivre.domain.exception;

public class EntidadeNaoEncontradaException extends RegraDeNegocioException {
    public EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
