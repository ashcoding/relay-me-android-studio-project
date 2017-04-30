package com.tinywebgears.relayme.oauth;

import org.scribe.model.Token;

import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.util.Pair;

public interface OauthHelper
{
    Pair<String, Token> findEmailAddress(Token accessToken) throws OperationFailedException;

    Pair<String, Token> buildXOAuthSmtp(String emailAddress, Token accessToken) throws OperationFailedException;

    Pair<String, Token> buildXOAuthImap(String emailAddress, Token accessToken) throws OperationFailedException;
}
