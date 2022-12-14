package mars18.restapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mars18.restapi.dto.*;
import mars18.restapi.entity.License;
import mars18.restapi.entity.PlayRecord;
import mars18.restapi.entity.User;
import mars18.restapi.exception.CustomException;
import mars18.restapi.repository.UnityRepository;
import mars18.restapi.repository.UserRepository;
import mars18.restapi.repository.WebLicenseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.constant.Constable;
import java.util.*;

import static mars18.restapi.exception.CustomErrorCode.*;
import static mars18.restapi.model.StatusTrue.COMPLETE_UPDATE_INFO;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppService {

    private final UnityRepository unityRepository;
    private final WebLicenseRepository webLicenseRepository;
    private final UserRepository userRepository;

    @Transactional
    public AppFeedbackDto.Response feedbackTapData(AppFeedbackDto.Request request) {
        FEEDBACKTAP_VALIDATION(request);

        return AppFeedbackDto.Response.response(
                PlayRecord.builder()
                        .name(unityRepository.findTopByNameOrderByIdDesc(request.getName()).getName())
                        .kind(unityRepository.findTopByNameOrderByIdDesc(request.getName()).getKind())
                        .score(unityRepository.findTopByNameOrderByIdDesc(request.getName()).getScore())
                        .build()
        );
    }

    @Transactional
    public List<Object> myPageTapData(AppMyDto.Request request) {
        //MYPAGETAP_VALIDATION(request);

        List<Object> parentList = new ArrayList<>();
        List<Map<Object, Object>> childList = new ArrayList<>();
        Map<Object, Object> noArrMap = new HashMap<>();

        noArrMap.put("name", request.getName());
        noArrMap.put("bartender", webLicenseRepository.findByName(request.getName()).getBartender());
        noArrMap.put("baker", webLicenseRepository.findByName(request.getName()).getBaker());

        // kind, ranking, score, play_at, playing_time

        List<PlayRecord> recordData = unityRepository.findByNameOrderByScoreDesc(request.getName());
        // ?????? ???????????? ????????? name??? ?????? ????????? ?????????(???)

        System.out.println("recordData.size() = " + recordData.size());
        for(int i = 0; i < recordData.size(); i++) {
            Map<Object, Object> arrMap = new HashMap<>();
            arrMap.put("kind", recordData.get(i).getKind());
            arrMap.put("ranking", i + 1);
            arrMap.put("score", recordData.get(i).getScore());
            arrMap.put("play_at", recordData.get(i).getCreateAt());
            arrMap.put("playing_time", recordData.get(i).getPlaying_time());

            childList.add(arrMap);
        }

        parentList.add(noArrMap);
        parentList.add(childList);

        return parentList;
    }

    @Transactional
    public Map<Object, Object> updateGetInfo(AppMyUpdateDto.Request request) {
        //GET_INFO_VALIDATION(request);

        Map<Object, Object> info = new HashMap<>();
        info.put("name", userRepository.findByName(request.getName()).getName());
        info.put("pw", userRepository.findByName(request.getName()).getPw());

        return info;
    }

    @Transactional
    public Constable updateInfo(AppMyUpdateDto.Request request) {
        INFO_UPDATE_VALIDATION(request);

        Optional<User> oUser = Optional.ofNullable(userRepository.findByName(request.getName())); // name??? ???????????? ????????? ??????
        if(oUser.isPresent()) { // oUser ????????? ???????????????
            User user = oUser.get();
            user.setName(request.getUpdateName());
            user.setPw(request.getUpdatePw());
            userRepository.save(user);
        }// db user table ????????? ?????? ????????? ?????? ????????? ??????

        Optional<License> oLicense = Optional.ofNullable(webLicenseRepository.findByName(request.getName())); // name??? ???????????? ????????? ??????
        if(oLicense.isPresent()) { // oUser ????????? ???????????????
            License license = oLicense.get();
            license.setName(request.getUpdateName());
            webLicenseRepository.save(license);
        }// db license table ????????? ?????? ????????? ?????? ????????? ??????

        List<PlayRecord> lPlayRecord = unityRepository.findByName(request.getName()); // name??? ???????????? ????????? ??????
            for (int i = 0; i < lPlayRecord.size(); i++) {
                PlayRecord playRecord = lPlayRecord.get(i);
                playRecord.setName(request.getUpdateName());
                unityRepository.save(playRecord);
            }
        // db play_record table ????????? ?????? ????????? ?????? ????????? ??????

        return COMPLETE_UPDATE_INFO;
    }

    // ?????? ??????

    private void FEEDBACKTAP_VALIDATION(AppFeedbackDto.Request request) {
        if (request.getName() == null)
        throw new CustomException(NULL_USER_NAME); // ?????? ????????? ??? (?????? ????????? ???????)

        if (!(unityRepository.existsNameByNameOrderByIdDesc(request.getName())))
            throw new CustomException(NOT_EXISTS_USER_RECORD); // ?????? ????????? ?????? ???
    }

    private void MYPAGETAP_VALIDATION(AppMyDto.Request request) {}

    private void INFO_UPDATE_VALIDATION(AppMyUpdateDto.Request request) {
        String name = request.getUpdateName();

        if (request.getUpdateName() == null)
            throw new CustomException(NULL_USER_UPDATE_NAME); // ?????? ????????? ???

        if (request.getUpdatePw() == null)
            throw new CustomException(NULL_USER_UPDATE_PW); // ???????????? ????????? ???

        if (!(request.getUpdateName().length() > 1 && request.getUpdateName().length() < 9))
            throw new CustomException(LIMIT_NAME_LENGTH); // 1 < ?????? ?????? < 9

        if (userRepository.existsByName(request.getUpdateName()))
            throw new CustomException(DUPLICATE_USER_NAME); // ?????? ??????

        if (name.contains("!") || name.contains("@") || name.contains("#") || name.contains("$")
                || name.contains("%") || name.contains("^") || name.contains("&")  || name.contains(")")
                || name.contains("*") || name.contains("(") || name.contains("0") )
            throw new CustomException(NO_CONTAINS_IN_NAME); // ?????? ?????? ?????? X

        if (!(request.getUpdatePw().length() > 5))
            throw new CustomException(PASSWORD_SIZE_ERROR); // ???????????? 6?????? ??????

        if (!(request.getUpdatePw().contains("!") || request.getUpdatePw().contains("@") || request.getUpdatePw().contains("#")
                || request.getUpdatePw().contains("$") || request.getUpdatePw().contains("%") || request.getUpdatePw().contains("^")
                || request.getUpdatePw().contains("&") || request.getUpdatePw().contains("*") || request.getUpdatePw().contains("(")
                || request.getUpdatePw().contains(")")))
            throw new CustomException(NOT_CONTAINS_EXCLAMATIONMARK); // ?????? ?????? ??????

    }
}
