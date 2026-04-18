package me.bchieu.base.modules.auth.domain.service;

@FunctionalInterface
public interface TokenIssuer {

  String issue(String username);
}
