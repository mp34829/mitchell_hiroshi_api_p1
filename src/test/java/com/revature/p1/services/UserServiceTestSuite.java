package com.revature.p1.services;

import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.datasource.documents.Batch;
import com.revature.p1.datasource.repos.BatchRepository;
import com.revature.p1.datasource.repos.UserRepository;
import com.revature.p1.util.PasswordUtils;
import com.revature.p1.util.exceptions.AuthenticationException;
import com.revature.p1.util.exceptions.InvalidRequestException;
import com.revature.p1.util.exceptions.ResourcePersistenceException;
import org.junit.*;

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
    private HttpSession mockSession;

    @Before
    public void beforeEachTest() {
        mockUserRepo = mock(UserRepository.class);
        mockSession = mock(HttpSession.class);
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
    public void isUserValid_returnsFalse_givenUserWithNullOrEmptyFirstName() {

        // Arrange
        AppUser invalidUser1 = new AppUser(null, "valid", "valid", "valid", "valid", "0");
        AppUser invalidUser2 = new AppUser("", "valid", "valid", "valid", "valid", "0");
        AppUser invalidUser3 = new AppUser("        ", "valid", "valid", "valid", "valid", "0");

        // Act
        boolean actualResult1 = sut.isUserValid(invalidUser1);
        boolean actualResult2 = sut.isUserValid(invalidUser2);
        boolean actualResult3 = sut.isUserValid(invalidUser3);

        /* Assert */
        Assert.assertFalse("User first name cannot be null!", actualResult1);
        Assert.assertFalse("User first name cannot be an empty string!", actualResult2);
        Assert.assertFalse("User first name cannot be only whitespace!", actualResult3);

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
    @Test(expected = InvalidRequestException.class)
    public void login_throwsException_whenGivenUsernameAsEmptyString() {
        // Arrange
        String emptyUsername = "";
        //Act
        try {
            sut.login(emptyUsername, java.util.UUID.randomUUID().toString());
        } finally  {
            // Assert
        }
    }

    @Test(expected = AuthenticationException.class)
    public void login_throwsException_whenGivenANonexistentUser(){
        // Arrange
        String encryptedPassword = "encrypted";
        AppUser expectedResult = null;
        AppUser invalidUser = new AppUser("first","last","email","username","password","0");
        when(mockPasswordUtil.generateSecurePassword(invalidUser.getPassword())).thenReturn(encryptedPassword);
        when(mockUserRepo.findUserByCredentials(invalidUser.getUsername(), encryptedPassword)).thenReturn(expectedResult);
        // Act
        sut.login(invalidUser.getUsername(), invalidUser.getPassword());
        // Assert
        verify(mockUserRepo, times(1)).findUserByCredentials(invalidUser.getUsername(), invalidUser.getPassword());
    }

   @Test
    public void removeBatch_removesBatchFromBatchRegistrationsForCurrentUser_whenBatchPassedAsArgument(){
        // Arrange

        AppUser user = new AppUser("first","last","email","username","password","0");
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
    public void enrollBatch_addsBatchToBatchRegistrationForCurrentUser_WhenBatchPassedAsArgument(){
        // Arrange
        AppUser user = new AppUser("first","last","email","username","password","0");
        Batch batch = new Batch("shortname", "name", "status", "description", Instant.now(), Instant.now());
        user.setBatchRegistrations(new ArrayList<String>());
        List<String> actualResult = user.getBatchRegistrations();
        List<String> desiredResult = new ArrayList<String>(Arrays.asList(batch.getShortName()));

        when(mockUserRepo.findUserByUsername(user.getUsername())).thenReturn(user);
        when(mockBatchRepo.findById(batch.getShortName())).thenReturn(batch);
        when(mockUserRepo.update(user, user.getUsername())).thenReturn(true);
        // Act
        sut.enrollBatch(user, batch.getShortName());
        // Assert
        Assert.assertEquals(actualResult, desiredResult);
        verify(mockUserRepo, times(1)).findUserByUsername(user.getUsername());
       verify(mockUserRepo, times(1)).update(user, user.getUsername());
    }
}
