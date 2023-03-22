package org.unitedinternet.cosmo.hibernate.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validator for Color
 * @author izidaru
 *
 */
public class ColorValidatorTest {
    
    /**
     *  7 digits
     */
    @Test
    public void colorMustHave7digits(){
        ColorValidator colorValidator = new ColorValidator();
        assertTrue(colorValidator.isValid("#123456",null), "color has 7 digits");
        assertFalse(colorValidator.isValid("#1234567",null), "color has 7 digits");
        assertFalse(colorValidator.isValid("#12345",null), "color has 7 digits");
    }
    
    /**
     * #
     */
    @Test
    public void colorMustStartWithDiez(){
        ColorValidator colorValidator = new ColorValidator();
        assertTrue(colorValidator.isValid("#123456",null), "color must start with #");
        assertFalse(colorValidator.isValid("1234567",null), "color must start with #");        
    }
    
    /**
     * hex
     */
    @Test
    public void onlyHexaDigitsPermited(){
        ColorValidator colorValidator = new ColorValidator();
        assertTrue(colorValidator.isValid("#123456",null), "color must start with #");
        assertTrue(colorValidator.isValid("#7890AB",null), "color must start with #");    
        assertTrue(colorValidator.isValid("#CDEFab",null), "color must start with #");    
        assertTrue(colorValidator.isValid("#cdef01",null), "color must start with #");    
        
        assertFalse(colorValidator.isValid("#Gdef01",null), "color must start with #");
    }
    
    /**
     *  null color
     */
    @Test
    public void nullColorIsValid(){
        ColorValidator colorValidator = new ColorValidator();
        assertTrue(colorValidator.isValid(null,null), "null color is valid");
        assertTrue(colorValidator.isValid("",null), "empty color is valid");
    }
    
}
