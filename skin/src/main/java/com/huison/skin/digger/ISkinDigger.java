package com.huison.skin.digger;

import com.huison.skin.kind.ISkin;

import java.util.Map;

/**
 * Created by huisonma on 2019/5/7.
 */
public interface ISkinDigger {

    Map<String, ISkin> getSkins();

    boolean isSkinValid(String skinName);
}
