package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.UserData;
import com.increff.ironic.pos.model.form.LoginForm;
import com.increff.ironic.pos.model.form.UserForm;
import com.increff.ironic.pos.pojo.UserPojo;
import com.increff.ironic.pos.service.UserService;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import com.increff.ironic.pos.testutils.MockUtils;
import com.increff.ironic.pos.util.ConversionUtil;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;

public class UserApiDtoTest extends AbstractUnitTest {

    @Autowired
    private UserApiDto userApiDto;

    @Autowired
    private UserService userService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void addSuccess() throws ApiException {
        UserForm userForm = MockUtils.getMockUserForm();
        UserPojo actual = userApiDto.add(userForm);
        UserPojo expected = userService.getByEmail(actual.getEmail());
        AssertUtils.assertEqualUsers(expected, actual);
    }

    @Test
    public void addWithInvalidPasswordThrowsException() throws ApiException {
        UserForm userForm = MockUtils.getMockUserForm();
        userForm.setPassword("WithoutSpecialChar");
        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Password must have at least one capital, small & special character!");
        userApiDto.add(userForm);
    }

    @Test
    public void addUserWithInvalidEmailThrowsException() throws ApiException {
        UserForm invalidUserForm = MockUtils.getMockUserForm();
        invalidUserForm.setEmail("invalid");

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Invalid email!");
        userApiDto.add(invalidUserForm);
    }

    @Test
    public void addUserWithInvalidPasswordThrowsException() throws ApiException {
        UserForm invalidUserForm = MockUtils.getMockUserForm();
        invalidUserForm.setPassword("");

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Password must have at least 8 characters!");
        userApiDto.add(invalidUserForm);
    }

    @Test
    public void deleteWithValidIdDeletesTheUser() throws ApiException {
        UserPojo mockUserPojo = MockUtils.getMockUser();
        userService.add(mockUserPojo);
        int id = mockUserPojo.getId();
        userApiDto.delete(id);
        boolean userExists = userService.getAll().stream().anyMatch(user -> user.getId().equals(id));
        Assert.assertFalse(userExists);
    }

    @Test
    public void deleteWithInValidIdThrowsApiException() throws ApiException {
        exceptionRule.expect(ApiException.class);
        int invalidId = -1;
        exceptionRule.expectMessage("No user found with ID: " + invalidId);
        userService.delete(invalidId);
    }

    @Test
    public void getAllUsersForNoUser() {
        List<UserData> userDataList = userApiDto.getAll();
        Assert.assertEquals(0, userDataList.size());
    }

    @Test
    public void getAllUsersForMultipleUsers() throws ApiException {
        List<UserData> expectedUsers = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            UserPojo userPojo = MockUtils.getMockUser();
            userPojo.setEmail("mock." + i + userPojo.getEmail());
            userService.add(userPojo);
            UserData userData = ConversionUtil.convertPojoToData(userPojo);
            expectedUsers.add(userData);
        }

        List<UserData> actualUserDataList = userApiDto.getAll();
        AssertUtils.assertEqualList(expectedUsers, actualUserDataList, AssertUtils::assertEqualUserData);
    }

    @Test
    public void getAuthenticatedUserGivesUserForValidInputs() throws ApiException {
        UserPojo userPojo = MockUtils.getMockUser();
        userService.add(userPojo);
        LoginForm loginForm = new LoginForm(userPojo.getEmail(), userPojo.getPassword());
        UserPojo actual = userApiDto.getAuthenticatedUser(loginForm);
        AssertUtils.assertEqualUsers(userPojo, actual);
    }

    @Test
    public void getAuthenticatedUserGivesUserForWrongPasswordThrowsException() throws ApiException {
        UserPojo userPojo = MockUtils.getMockUser();
        userService.add(userPojo);
        LoginForm loginForm = new LoginForm(userPojo.getEmail(), "wrong password");

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Invalid email or password");
        userApiDto.getAuthenticatedUser(loginForm);
    }

}
