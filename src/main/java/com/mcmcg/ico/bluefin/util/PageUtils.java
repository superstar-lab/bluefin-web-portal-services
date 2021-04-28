package com.mcmcg.ico.bluefin.util;

import org.springframework.data.domain.PageRequest;

public class PageUtils {

    public static PageRequest getPageRequest(int page, int size) {
         return PageRequest.of(page, size);

    }
}
