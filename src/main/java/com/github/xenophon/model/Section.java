package com.github.xenophon.model;

import java.util.Collection;

public interface Section<T> {

    String getTitle();

    Collection<T> getItems();
}
