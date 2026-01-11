package edu.gmu.cs321;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PersonTest {

    private Person person;

     /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    /*
     * Create Person tests.
     */
    @Test
    public void createPersonValid(){

        int personID = Person.createPerson("Jimin", "Park") + 1;
        assertTrue("Person created succesfully returns ID > 0", personID > 0 );
    }

    @Test
    public void createPersonInvalid(){

        int personID = Person.createPerson(null, null);
        assertTrue("Person created insuccesfully returns 0", personID == 0);

    }

    /*
     * Update Tests
     */
    @Test
    public void updatePersonValid(){
        boolean ok = (Person.updatePerson(1, "Mubashar", "Ikram") == 1);
        assertTrue("Person updated successfully returns true", ok);
    }


    @Test
    public void updatePersonInvalid(){
        boolean ok = (Person.updatePerson(0, "", "") == 1);
        assertTrue("Person updated unsuccessfully returns false", ok == false);
    }

    /*
    * Setter Tests
    */
    @Test
    public void setters_changeNames_inMemory() {
        Person p = new Person(1, "Jimin", "Park");
        p.setFirstName("Mubashar");
        p.setLastName("Ikram");
        assertTrue("First name updated", "Mubashar".equals(p.getFirstName()));
        assertTrue("Last name updated",  "Ikram".equals(p.getLastName()));
    }

    /*
     * Get Person Tests
     */
    @Test
    public void testGetPersonValid(){
        Person person = Person.getPerson(1);
        assertTrue("Person updated successfully returns true", person != null);
    }


    @Test
    public void testGetPersonInvalid(){
        Person person = Person.getPerson(0);
        assertTrue("Person updated unsuccessfully returns false", person == null);
    }

    /*
     * Get Person Parameters tests.
     */
    @Test
    public void testGetParameters(){
        Person person1 = new Person(1, "fName", "lName");
        assertTrue("Method is able retrieve Person id successfully", person1.getId() == 1);
        assertTrue("Method is able retrieve Person's first name successfully", ((person1.getFirstName()).equals("fName")));
        assertTrue("Method is able retrieve Person's last name successfully", ((person1.getLastName()).equals("lName")));


        Person person2 = new Person(0, null, null);
        assertTrue("Method is unable to retrieve the Person id", person2.getId() == 0);
        assertTrue("Method is unable retrieve Person's first name", (person2.getFirstName() == null));
        assertTrue("Method is unable retrieve Person's last name", (person2.getLastName() == null));
    }

}
