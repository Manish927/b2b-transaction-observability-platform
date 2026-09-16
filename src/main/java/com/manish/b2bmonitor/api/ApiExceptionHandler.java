package com.manish.b2bmonitor.api;

import com.manish.b2bmonitor.as2.InvalidAs2RequestException;
import com.manish.b2bmonitor.as2.As2MessageConflictException;
import com.manish.b2bmonitor.as2.UnknownTradingPartnerException;
import com.manish.b2bmonitor.service.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(TransactionNotFoundException.class)
    public ProblemDetail handleNotFound(TransactionNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Transaction not found");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(InvalidAs2RequestException.class)
    public ProblemDetail handleBadRequest(InvalidAs2RequestException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid AS2 request");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(UnknownTradingPartnerException.class)
    public ProblemDetail handleUnknownPartner(UnknownTradingPartnerException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Trading partner not authorized");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(As2MessageConflictException.class)
    public ProblemDetail handleMessageConflict(As2MessageConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("AS2 Message-ID conflict");
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
