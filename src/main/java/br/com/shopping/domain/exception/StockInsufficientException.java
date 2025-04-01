package br.com.shopping.domain.exception;

public class StockInsufficientException extends RuntimeException {
    
    public StockInsufficientException(String message) {
        super(message);
    }
    
    public StockInsufficientException(String message, Throwable cause) {
        super(message, cause);
    }
}