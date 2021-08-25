package com.revature.p1.services;

import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.datasource.documents.Batch;
import com.revature.p1.datasource.repos.BatchRepository;
import com.revature.p1.datasource.repos.UserRepository;
import com.revature.p1.util.PasswordUtils;
import com.revature.p1.util.exceptions.AuthenticationException;
import com.revature.p1.util.exceptions.InvalidRequestException;
import com.revature.p1.util.exceptions.ResourcePersistenceException;
import org.bson.json.JsonObject;
import org.json.simple.JSONObject;
import org.junit.*;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTestSuite {

    UserService sut;

    private PasswordUtils passwordUtils;
    private UserRepository mockUserRepo;
    private BatchRepository mockBatchRepo;
    private PasswordUtils mockPasswordUtil;

    @Before
    public void beforeEachTest() {
        mockUserRepo = mock(UserRepository.class);
        mockPasswordUtil = mock(PasswordUtils.class);
        mockBatchRepo = mock(BatchRepository.class);
        sut = new UserService(mockUserRepo, mockBatchRepo, mockPasswordUtil);
    }

    @After
    public void afterEachTest() {
        sut = null;
    }

    @Test
    public void isUserValid_returnsTrue_givenValidUser() {

        // Arrange
        AppUser validUser = new AppUser("valid", "valid", "valid", "valid", "valid", "0");

        // Act
        boolean actualResult = sut.isUserValid(validUser);

        // Assert
        Assert.assertTrue("Expected user to be considered valid!", actualResult);

    }

    @Test
    public void isUserValid_returnsFalse_givenUserWithNullOrEmptyCredentials() {

        // Arrange
        AppUser invalidUser1 = new AppUser(null, "valid", "valid", "valid", "valid", "0");
        AppUser invalidUser2 = new AppUser("", "valid", "valid", "valid", "valid", "0");
        AppUser invalidUser3 = new AppUser("        ", "valid", "valid", "valid", "valid", "0");
        AppUser invalidUser4 = new AppUser("valid", null, "valid", "valid", "valid", "0");
        AppUser invalidUser5 = new AppUser("valid", "valid", null, "valid", "valid", "0");
        AppUser invalidUser6 = new AppUser("valid", "valid", "valid", null, "valid", "0");
        AppUser invalidUser7 = new AppUser("valid", "valid", "valid", "valid", null, "0");

        // Act
        boolean actualResult1 = sut.isUserValid(invalidUser1);
        boolean actualResult2 = sut.isUserValid(invalidUser2);
        boolean actualResult3 = sut.isUserValid(invalidUser3);
        boolean actualResult4 = sut.isUserValid(invalidUser3);
        boolean actualResult5 = sut.isUserValid(invalidUser3);
        boolean actualResult6 = sut.isUserValid(invalidUser3);
        boolean actualResult7 = sut.isUserValid(invalidUser3);

        /* Assert */
        Assert.assertFalse("User first name cannot be null!", actualResult1);
        Assert.assertFalse("User first name cannot be an empty string!", actualResult2);
        Assert.assertFalse("User first name cannot be only whitespace!", actualResult3);
        Assert.assertFalse("User last name cannot be null!", actualResult4);
        Assert.assertFalse("User email cannot be null!", actualResult5);
        Assert.assertFalse("User username cannot be null!", actualResult6);
        Assert.assertFalse("User password cannot be null!", actualResult7);
    }

    @Test
    public void register_returnsSuccessfully_whenGivenValidUser() {

        // Arrange
        AppUser expectedResult = new AppUser("valid", "valid", "valid", "valid", "valid", "0");
        AppUser validUser = new AppUser("valid", "valid", "valid", "valid", "valid", "0");
        when(mockUserRepo.save(any())).thenReturn(expectedResult);

        // Act
        AppUser actualResult = sut.register(validUser);

        // Assert
        Assert.assertEquals(expectedResult, actualResult);
        verify(mockUserRepo, times(1)).save(any());

    }

    @Test(expected = InvalidRequestException.class)
    public void register_throwsException_whenGivenInvalidUser() {

        // Arrange
        AppUser invalidUser = new AppUser(null, "", "", "", "", "");

        // Act
        try {
            sut.register(invalidUser);
        } finally {
            // Assert
            verify(mockUserRepo, times(0)).save(any());
        }

    }

    @Test(expected = ResourcePersistenceException.class)
    public void register_throwsException_whenGivenUserWithDuplicateUsername() {

        // Arrange
        AppUser existingUser = new AppUser("original", "original", "original", "duplicate", "original", "0");
        AppUser duplicate = new AppUser("first", "last", "email", "duplicate", "password", "0");

        when(mockUserRepo.findUserByUsername(duplicate.getUsername())).thenReturn(existingUser);

        // Act
        try {
            sut.register(duplicate);
        } finally {
            // Assert
            verify(mockUserRepo, times(1)).findUserByUsername(duplicate.getUsername());
            verify(mockUserRepo, times(0)).save(duplicate);
        }

    }
    @Test(expected = ResourcePersistenceException.class)
    public void register_throwsException_whenGivenUserWithDuplicateEmail() {

        // Arrange
        AppUser existingUser = new AppUser("original", "original", "duplicate", "original", "original", "0");
        AppUser duplicate = new AppUser("first", "last", "duplicate", "username", "password", "0");

        when(mockUserRepo.findUserByEmail(duplicate.getEmail())).thenReturn(existingUser);

        // Act
        try {
            sut.register(duplicate);
        } finally {
            // Assert
            verify(mockUserRepo, times(1)).findUserByUsername(duplicate.getUsername());
            verify(mockUserRepo, times(0)).save(duplicate);
        }

    }

    @Test(expected = InvalidRequestException.class)
    public void login_throwsException_whenGivenUsernameAsEmptyString() {
        // Arrange
        String emptyUsername = "";
        //Act
        try {
            sut.login(emptyUsername, java.util.UUID.randomUUID().toString());
        } finally {
            // Assert
        }
    }

    @Test(expected = AuthenticationException.class)
    public void login_throwsException_whenGivenANonexistentUser() {
        // Arrange
        String encryptedPassword = "encrypted";
        AppUser expectedResult = null;
        AppUser invalidUser = new AppUser("first", "last", "email", "username", "password", "0");
        when(mockPasswordUtil.generateSecurePassword(invalidUser.getPassword())).thenReturn(encryptedPassword);
        when(mockUserRepo.findUserByCredentials(invalidUser.getUsername(), encryptedPassword)).thenReturn(expectedResult);
        // Act
        sut.login(invalidUser.getUsername(), invalidUser.getPassword());
        // Assert
        verify(mockUserRepo, times(1)).findUserByCredentials(invalidUser.getUsername(), invalidUser.getPassword());
    }

    @Test
    public void removeBatch_removesBatchFromBatchRegistrationsForCurrentUser_whenBatchPassedAsArgument() {
        // Arrange

        AppUser user = new AppUser("first", "last", "email", "username", "password", "0");
        Batch batch = new Batch("shortname", "name", "status", "description", Instant.now(), Instant.now());
        List<AppUser> batchRegistration = new ArrayList<AppUser>(Arrays.asList(user));

        when(mockUserRepo.findUsersByBatch(batch.getShortName())).thenReturn(batchRegistration);
        when(mockBatchRepo.findById(batch.getShortName())).thenReturn(batch);
        when(mockUserRepo.update(user, user.getUsername())).thenReturn(true);
        // Act
        sut.removeBatch(batch.getShortName());
        // Assert
        Assert.assertTrue(user.getBatchRegistrations().isEmpty());
    }

    @Test
    public void enrollBatch_addsBatchToBatchRegistrationForCurrentUser_WhenBatchPassedAsArgument() {
        // Arrange
        AppUser user = new AppUser("first", "last", "email", "username", "password", "0");
        Batch batch = new Batch("shortname", "name", "status", "description", Instant.now(), Instant.now());
        user.setBatchRegistrations(new ArrayList<String>());
        List<String> expectedResult = new ArrayList<String>(Arrays.asList(batch.getShortName()));
        List<String> actualResult = user.getBatchRegistrations();

        when(mockBatchRepo.findById(batch.getShortName())).thenReturn(batch);
        when(mockUserRepo.update(user, user.getUsername())).thenReturn(true);
        // Act
        sut.enrollBatch(user, batch.getShortName());
        // Assert
        Assert.assertEquals(expectedResult, actualResult);
        verify(mockUserRepo, times(1)).update(user, user.getUsername());
    }

    @Test
    public void withdrawBatch_withdrawsBatch_whenGivenValidBatch(){
        AppUser user = new AppUser("first","last","email","username","password","0");
        List<String> batchRegistrations = new ArrayList<>(Arrays.asList("shortname"));
        user.setBatchRegistrations(batchRegistrations);
        List<String> expectedResult = Arrays.asList();
        List<String> actualResult = user.getBatchRegistrations();
        when(mockUserRepo.update(user,user.getUsername())).thenReturn(true);

        sut.withdrawBatch(user, "shortname");

        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void updateUserByField_updatesFields_whenFieldsUpdated(){
        AppUser user = new AppUser("first", "last", "email", "username", "password", "0");
        JSONObject json = new JSONObject();
        json.put("firstName", "edit");
        json.put("lastName", "edit");
        json.put("email", "edit");

        when(mockUserRepo.update(user, user.getUsername())).thenReturn(true);

        sut.updateUserByField(user, json);

        Assert.assertEquals(user.getFirstName(), "edit");
        Assert.assertEquals(user.getLastName(), "edit");
        Assert.assertEquals(user.getEmail(), "edit");
    }

    @Test(expected = InvalidRequestException.class)
    public void updateUserByField_throwsException_WhenInvalidFieldUpdateRequested() {
        AppUser user = new AppUser("first", "last", "email", "username", "password", "0");
        JSONObject json = new JSONObject();
        json.put("invalid", "edit");
        json.put("lastName", "edit");
        json.put("email", "edit");

        when(mockUserRepo.update(user, user.getUsername())).thenReturn(true);

        sut.updateUserByField(user, json);

        verify(mockUserRepo, times(1)).update(user, user.getUsername());
    }

}
