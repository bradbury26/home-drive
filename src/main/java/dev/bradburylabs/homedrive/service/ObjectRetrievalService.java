package dev.bradburylabs.homedrive.service;

import dev.bradburylabs.homedrive.model.object.RetrieveObjectRequest;
import dev.bradburylabs.homedrive.model.object.RetrieveObjectResponse;

public interface ObjectRetrievalService<T extends RetrieveObjectRequest> {
    RetrieveObjectResponse retrieveObject(T objectRetrievalRequest, boolean retrieveInputStream);
}
