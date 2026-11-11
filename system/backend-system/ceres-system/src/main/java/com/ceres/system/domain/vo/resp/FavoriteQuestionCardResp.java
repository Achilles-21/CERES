package com.ceres.system.domain.vo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class FavoriteQuestionCardResp {

    private Long favoriteId;

    private Long queId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date favoriteTime;

    private Long courseId;

    private String type;

    private String description;

    private String content;

    private String choiceA;

    private String choiceB;

    private String choiceC;

    private String choiceD;

    private String choiceE;

    private String choiceF;

    private String answer;

    private String answerExplain;

    private String imgUrl;

    private String imgId;

    private String favoriteFlag = "1";

    private Boolean disabled = false;

    private List<ChoiceOptions> options = new ArrayList<>();

    public void refreshOptions() {
        List<ChoiceOptions> refreshedOptions = new ArrayList<>();
        addOption(refreshedOptions, "A", choiceA);
        addOption(refreshedOptions, "B", choiceB);
        addOption(refreshedOptions, "C", choiceC);
        addOption(refreshedOptions, "D", choiceD);
        addOption(refreshedOptions, "E", choiceE);
        addOption(refreshedOptions, "F", choiceF);
        options = refreshedOptions;
    }

    private void addOption(List<ChoiceOptions> refreshedOptions, String title, String choice) {
        if (choice != null && choice.trim().length() > 0) {
            refreshedOptions.add(new ChoiceOptions(title, choice));
        }
    }
}
