package com.baswara.generic.web.rest.errors;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class MetaClassNotFoundException extends AbstractThrowableProblem {

    public MetaClassNotFoundException() {
        super(ErrorConstants.META_NOT_FOUND_TYPE, "Class is not registered on Meta", Status.BAD_REQUEST);
    }
}
