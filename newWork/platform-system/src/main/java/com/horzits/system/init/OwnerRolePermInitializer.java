package com.horzits.system.init;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import com.horzits.system.service.ISysRoleService;
import com.horzits.system.service.ISysMenuService;
import com.horzits.common.core.domain.entity.SysRole;
import com.horzits.common.core.domain.entity.SysMenu;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class OwnerRolePermInitializer implements ApplicationRunner {
  @Autowired
  private ISysRoleService roleService;
  @Autowired
  private ISysMenuService menuService;

  @Override
  public void run(ApplicationArguments args) {
    try {
      SysRole query = new SysRole();
      query.setRoleKey("owner");
      List<SysRole> roles = roleService.selectRoleList(query);
      SysRole owner = null;
      if (roles != null && !roles.isEmpty()) {
        owner = roles.get(0);
      } else {
        List<SysRole> all = roleService.selectRoleAll();
        if (all != null) {
          for (SysRole r : all) {
            if ("owner".equalsIgnoreCase(r.getRoleKey())) {
              owner = r;
              break;
            }
          }
        }
      }
      if (owner == null) {
        owner = new SysRole();
        owner.setRoleName("业主");
        owner.setRoleKey("owner");
        owner.setStatus("0");
        owner.setRemark("业主角色");
        List<SysMenu> menus = menuService.selectMenuList(new SysMenu(), 1L);
        List<Long> ids = collectOwnerMenuIds(menus);
        owner.setMenuIds(ids.toArray(new Long[0]));
        roleService.insertRole(owner);
      } else {
        List<SysMenu> menus = menuService.selectMenuList(new SysMenu(), 1L);
        List<Long> ids = collectOwnerMenuIds(menus);
        owner.setMenuIds(ids.toArray(new Long[0]));
        roleService.updateRole(owner);
      }
    } catch (Exception ignored) {
    }
  }

  private List<Long> collectOwnerMenuIds(List<SysMenu> menus) {
    if (menus == null)
      return new ArrayList<>();
    List<String> need = new ArrayList<>();
    need.add("business:repairCategory:list");
    need.add("business:repairCategory:query");
    need.add("business:repairOrder:add");
    return menus.stream()
        .filter(m -> m.getPerms() != null && need.contains(m.getPerms()))
        .map(SysMenu::getMenuId)
        .distinct()
        .collect(Collectors.toList());
  }
}
