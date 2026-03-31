package com.fractalmindstudio.minerva_core.model.identity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.AssertionErrors;

@ExtendWith(MockitoExtension.class)
public class UserTest {

    @Test
    public void emptyUser(){
        User u = new User();

        AssertionErrors.assertNull("Name", u.getName());
        AssertionErrors.assertNull("Last name", u.getLastName());
        AssertionErrors.assertNull("Address", u.getAddress());
    }

    @Test
    public void gettersAndSettersSameValuesUser(){
        User u = new User();

        u.setName("name1");
        u.setLastName("lastname2");
        u.setAddress("address3");

        AssertionErrors.assertEquals("Name", "name1", u.getName());
        AssertionErrors.assertEquals("Last name", "lastname2", u.getLastName());
        AssertionErrors.assertEquals("Address", "address3", u.getAddress());
    }
}
