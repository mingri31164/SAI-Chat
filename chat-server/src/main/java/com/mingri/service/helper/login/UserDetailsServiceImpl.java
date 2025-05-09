package com.mingri.service.helper.login;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mingri.constant.MessageConstant;
import com.mingri.entity.login.LoginUser;
import com.mingri.entity.sys.SysUser;
import com.mingri.exception.LoginFailedException;
import com.mingri.mapper.SysMenuMapper;
import com.mingri.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysMenuMapper sysMenuMapper;


    /**
     * @Description: 重写security过滤器中的方法，改为从数据库查询用户信息
     * @Author: mingri31164
     * @Date: 2025/1/22 22:57
     **/
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("到这了333333");
        //根据用户名查询数据库中的数据
        SysUser sysUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username)
        );
        if(Objects.isNull(sysUser)){
            throw new LoginFailedException(MessageConstant.LOGIN_ERROR);
        }

        //把查询到的user结果，封装成UserDetails类型，然后返回。
        //但是由于UserDetails是个接口，所以我们需要先新建LoginUser类，作为UserDetails的实现类

        // 查询用户权限信息
        //权限集合，在LoginUser类做权限集合的转换
//        List<String> list = new ArrayList<>(Arrays.asList("test","admin","user"));
        List<String> list = sysMenuMapper.selectPermsByUserId(sysUser.getId());

        //把查询到的user结果，封装成UserDetails类型返回
        return new LoginUser(sysUser,list); //这里传了第二个参数，表示的是权限信息
    }





}
