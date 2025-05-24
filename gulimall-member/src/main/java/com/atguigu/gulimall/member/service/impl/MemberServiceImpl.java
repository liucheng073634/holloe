package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExsitException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import com.atguigu.gulimall.member.vo.UserMemberVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao  memberLevelDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    //  1.检查用户名和手机号是否唯一
    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity memberEntity = new MemberEntity();
        // 获取默认会员等级
       MemberLevelEntity   memberLevelEntity=memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId());
        //检查用户名和手机号是否唯一
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        memberEntity.setPassword(bCryptPasswordEncoder.encode(vo.getPassword()));
        //其他信息


        // 手机号
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setNickname(vo.getUserName());
         baseMapper.insert(memberEntity);
    }

    @Override
    public void checkUserNameUnique(String userName) throws UsernameExistException {
        Integer username = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (username>0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExsitException{
        Integer mobile = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            throw new PhoneExsitException();
        }


    }

    @Override
    public MemberEntity login(UserMemberVo userMemberVo) {
        String loginacct = userMemberVo.getLoginAccount();
        String password = userMemberVo.getPassword();
        MemberDao baseMapper1 = this.baseMapper;
        MemberEntity memberEntity = baseMapper1.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));

        if(memberEntity==null){
        return null;
        }else{
            //  密码匹配
            String password1 = memberEntity.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(password, password1);
            if(matches){
                return memberEntity;
            }else {
                return null;
            }
        }

    }

    @Override
    public MemberEntity login(SocialUser socialUser) {
        String uid = socialUser.getUid();
        MemberEntity memberEntity1 = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if(memberEntity1!=null){
            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setId(memberEntity1.getId());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            memberEntity.setAccessToken(socialUser.getAccess_token());
            this.baseMapper.updateById(memberEntity);
            memberEntity1.setExpiresIn(socialUser.getExpires_in());
            memberEntity1.setAccessToken(socialUser.getAccess_token());
            return memberEntity1;
        }else{
            MemberEntity regist = new MemberEntity();
            try {
                     Map<String, String> query = new HashMap<>();
                     query.put("access_token",socialUser.getAccess_token());
                     query.put("uid",socialUser.getUid());

                HttpResponse get = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "GET", new HashMap<String, String>(), query);
                if(get.getStatusLine().getStatusCode() ==200){
                        // 获取用户信息
                    String string = EntityUtils.toString(get.getEntity());
                    JSONObject jsonObject = JSON.parseObject(string);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    regist.setNickname(name);
                    regist.setGender("m".equals(gender) ? 1 :0);

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

                regist.setSocialUid(socialUser.getUid());
                regist.setAccessToken(socialUser.getAccess_token());
                regist.setExpiresIn(socialUser.getExpires_in());
            this.baseMapper.insert(regist);
        return regist;
        }

    }


}








