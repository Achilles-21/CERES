package com.ceres.system.domain.vo.resp;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FavoriteQuestionCardRespTest {

    @Test
    void refreshOptionsKeepsOnlyNonBlankChoicesInQuestionOrder() {
        FavoriteQuestionCardResp resp = new FavoriteQuestionCardResp();
        resp.setChoiceA("First option");
        resp.setChoiceB("");
        resp.setChoiceC("   ");
        resp.setChoiceD("Fourth option");
        resp.setChoiceE(null);
        resp.setChoiceF("Sixth option");

        resp.refreshOptions();

        List<ChoiceOptions> options = resp.getOptions();
        assertEquals(3, options.size());
        assertEquals("A", options.get(0).getTitle());
        assertEquals("First option", options.get(0).getChoice());
        assertEquals("D", options.get(1).getTitle());
        assertEquals("Fourth option", options.get(1).getChoice());
        assertEquals("F", options.get(2).getTitle());
        assertEquals("Sixth option", options.get(2).getChoice());
    }
}
