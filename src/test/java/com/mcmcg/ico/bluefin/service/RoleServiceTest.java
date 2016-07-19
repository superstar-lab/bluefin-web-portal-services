package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.jpa.RoleRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.util.TestUtilClass;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class RoleServiceTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    @Autowired
    private RoleService roleService;

    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);

        // http://kim.saabye-pedersen.org/2012/12/mockito-and-spring-proxies.html
        // This issue is fixed in spring version 4.3.1, but spring boot
        // 3.6-RELEASE supports 4.2.7
        RoleService rService = (RoleService) TestUtilClass.unwrapProxy(roleService);

        ReflectionTestUtils.setField(rService, "roleRepository", roleRepository);
    }

    @Test
    public void testFindAllSuccess() {
        List<Role> roleList = new ArrayList<Role>();
        roleList.add(new Role());
        Mockito.when(roleRepository.findAll()).thenReturn(roleList);
        roleList = roleService.getRoles();

        Assert.assertFalse(roleList.isEmpty());
        Mockito.verify(roleRepository, Mockito.times(1)).findAll();
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testFindAllFail() {
        Mockito.when(roleRepository.findAll())
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        roleService.getRoles();
        Mockito.verify(roleRepository, Mockito.times(1)).findAll();
    }

    /**
     * Test when we send the correct data
     */
    @Test
    public void testGetRolesByIdsByIds() {
        List<Role> mockedLoadedRoles = getValidRoleList();
        Mockito.when(roleRepository.findAll(Mockito.anyCollectionOf(Long.class))).thenReturn(mockedLoadedRoles);

        Set<Long> expectedRoleIds = new HashSet<Long>(Arrays.asList(1L, 2L, 3L));
        List<Role> loadedRoles = roleService.getRolesByIds(expectedRoleIds);

        Assert.assertEquals(expectedRoleIds.size(), loadedRoles.size());
        Assert.assertTrue(loadedRoles.stream().filter(x -> !expectedRoleIds.contains(x.getRoleId()))
                .collect(Collectors.toSet()).isEmpty());

        Mockito.verify(roleRepository, Mockito.times(1)).findAll(Mockito.anyCollectionOf(Long.class));
        Mockito.verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Test when the system does not have roles
     */
    @Test(expected = CustomBadRequestException.class)
    public void testGetRolesByIdsEmptyList() {
        Mockito.when(roleRepository.findAll(Mockito.anyCollectionOf(Long.class))).thenReturn(new ArrayList<Role>());

        roleService.getRolesByIds(new HashSet<Long>(Arrays.asList(1L, 2L, 3L)));

        Mockito.verify(roleRepository, Mockito.times(1)).findAll(Mockito.anyCollectionOf(Long.class));
        Mockito.verifyNoMoreInteractions(roleRepository);
    }

    /**
     * Test when we pass a wrong role ids
     */
    @Test
    public void testGetRolesByIdsOneWrongElement() {
        Mockito.when(roleRepository.findAll(Mockito.anyCollectionOf(Long.class))).thenReturn(getValidRoleList());
        expectedEx.expect(CustomBadRequestException.class);
        expectedEx.expectMessage("The following roles don't exist.  List = [5, 7]");

        roleService.getRolesByIds(new HashSet<Long>(Arrays.asList(1L, 2L, 3L, 5L, 7L)));

        Mockito.verify(roleRepository, Mockito.times(1)).findAll(Mockito.anyCollectionOf(Long.class));
        Mockito.verifyNoMoreInteractions(roleRepository);
    }

    private List<Role> getValidRoleList() {
        List<Role> roleList = new ArrayList<Role>();
        roleList.add(createValidRole());

        Role role = new Role();
        role.setRoleId(2L);
        role.setRoleName("DEV");
        role.setDescription("DEV");
        roleList.add(role);

        role = new Role();
        role.setRoleId(3L);
        role.setRoleName("USER");
        role.setDescription("USER");
        roleList.add(role);

        return roleList;
    }

    private Role createValidRole() {
        Role role = new Role();
        role.setRoleId(1L);
        role.setRoleName("ADMIN");
        role.setDescription("ADMIN");
        return role;
    }
}
