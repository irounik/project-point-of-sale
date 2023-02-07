package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.UserDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.auth.UserRole;
import com.increff.ironic.pos.pojo.UserPojo;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import com.increff.ironic.pos.testutils.MockUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

public class UserServiceTest extends AbstractUnitTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserService userService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testAddUser() throws ApiException {
        UserPojo userPojo = MockUtils.getMockUser();
        userService.add(userPojo);
        UserPojo actual = userDao.selectByEmail(userPojo.getEmail());
        AssertUtils.assertEqualUsers(userPojo, actual);
    }

    @Test
    public void testAddAdminUser() throws ApiException {
        UserPojo userPojo = MockUtils.getMockUser();
        userPojo.setEmail("admin@test.com");
        userPojo.setRole(UserRole.NONE);
        userService.add(userPojo);
        UserPojo actual = userDao.selectByEmail(userPojo.getEmail());
        AssertUtils.assertEqualUsers(userPojo, actual);
        Assert.assertEquals(actual.getRole(), UserRole.SUPERVISOR);
    }

    @Test
    public void testSelectByEmailForInvalidEmail() throws ApiException {
        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("No user found with email");
        userService.getByEmail("invalid@email.com");
    }

    @Test
    public void testSelectByEmail() throws ApiException {
        UserPojo mockUserPojo = MockUtils.getMockUser();
        userDao.insert(mockUserPojo);
        UserPojo actual = userService.getByEmail(mockUserPojo.getEmail());
        AssertUtils.assertEqualUsers(mockUserPojo, actual);
    }

    @Test
    public void addingDuplicateUserThrowsApiException() throws ApiException {
        UserPojo original = MockUtils.getMockUser();
        userDao.insert(original);
        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("User with given email already exists");
        UserPojo duplicate = MockUtils.getMockUser();
        userService.add(duplicate);
    }

    @Test
    public void testGetAllUsers() {
        List<UserPojo> expectedUserEntities = Arrays.asList(
                new UserPojo(null, "email1@mail.com", "Pass1", UserRole.OPERATOR),
                new UserPojo(null, "email2@mail.com", "Pass2", UserRole.SUPERVISOR),
                new UserPojo(null, "email3@mail.com", "Pass2", UserRole.OPERATOR)
        );
        expectedUserEntities.forEach(userDao::insert);

        List<UserPojo> actualUserEntities = userService.getAll();
        AssertUtils.assertEqualList(expectedUserEntities, actualUserEntities, AssertUtils::assertEqualUsers);
    }

    @Test
    public void deleteUserWithValidId() throws ApiException {
        UserPojo mockUserPojo = MockUtils.getMockUser();
        userDao.insert(mockUserPojo);
        userService.delete(mockUserPojo.getId());

        boolean userStillExists = userDao
                .selectAll()
                .stream()
                .anyMatch(user -> user.getId().equals(mockUserPojo.getId()));

        Assert.assertFalse(userStillExists);
    }

    @Test
    public void deleteUserWithInvalidIdThrowsException() throws ApiException {
        exceptionRule.expect(ApiException.class);
        int invalidId = -1;
        exceptionRule.expectMessage("No user found with ID: " + invalidId);
        userService.delete(invalidId);
    }

}
