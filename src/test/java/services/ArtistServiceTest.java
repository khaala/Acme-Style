package services;

import domain.Artist;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import utilities.AbstractTest;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Null;


@Transactional
@ContextConfiguration(locations = {
        "classpath:spring/junit.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtistServiceTest extends AbstractTest {

    // The SUT
    // ====================================================

    @Autowired
    private ArtistService artistService;

    @Autowired
    private UserAccountService userAccountService;

    // Tests
    // ====================================================

     /*  FUNCTIONAL REQUIREMENT:
            * An actor who is not authenticated must be able to:
               -. Register to the system as a stylist.
               -. Register to the system as a makeup artist.
               -. Register to the system as a photographer.
               -. Login to the system using his/her credentials.
    */
    public void artistRegisterLoginTest(final String username, final String password, final String passwordRepeat, final String name, final String surname, final String phone, final String email, final String postalAddress,  final Class<?> expected) {
        Class<?> caught = null;
        startTransaction();
        try {

            final Artist result;

            result = this.artistService.create();

            result.setUserAccount(this.userAccountService.create("STYLIST"));
            result.getUserAccount().setUsername(username);
            result.setName(name);
            result.setSurname(surname);
            result.setPhone(phone);
            result.setEmail(email);
            result.setPostalAddresses(postalAddress);

            result.getUserAccount().setPassword(new Md5PasswordEncoder().encodePassword(password, null));

            this.artistService.save(result);
            artistService.flush();

            this.authenticate(username);

        } catch (final Throwable oops) {

            caught = oops.getClass();

        }

        this.checkExceptions(expected, caught);
        rollbackTransaction();

    }

    //Drivers
    // ===================================================

    @Test
    public void driverArtistRegisterLoginTest() {

        final Object testingData[][] = {

                // Alguien sin registrar/logueado como stylist -> true
                {
                        "stylist33", "stylist33", "stylist33", "stylist33TestName", "stylist33TestSurname", "+34 123456789", "stylist33Test@stylist33Test.com", "addressStylist33",  null
                },
//                // Alguien sin registrar/logueado como makeup artist -> true
//                {
//                        "makeup33", "makeup33", "makeup33", "makeup33TestName", "makeup33TestSurname", "+34 123456789", "makeup33Test@makeup33Test.com", "addressMakeup33",  null
//                },
//                // Alguien sin registrar/logueado como photographer -> true
//                {
//                        "photographer33", "photographer33", "photographer33", "photographer33TestName", "photographer33TestSurname", "+34 123456789", "photographer33Test@photographer33Test.com", "addressPhotographer33",  null
//                },
                // Todos los campos como null --> false
                {
                        null, null, null, null, null, null, null, null, ConstraintViolationException.class
                },
                // Todos los campos completados, introduciendo un <script> en el nombre -> false
                {
                        "stylist33", "stylist33", "stylist33", "<script>", "stylist33TestSurname43","+34 123456789", "stylist33Test@stylist33Test.com", "adressStylist",  ConstraintViolationException.class
                }

        };
        for (int i = 0; i < testingData.length; i++)
            this.artistRegisterLoginTest((String) testingData[i][0], (String) testingData[i][1], (String) testingData[i][2], (String) testingData[i][3], (String) testingData[i][4], (String) testingData[i][5], (String) testingData[i][6],
                    (String) testingData[i][7], (Class<?>) testingData[i][8]);
    }
}
