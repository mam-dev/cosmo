package org.unitedinternet.cosmo.hibernate.validator;


import org.junit.Assert;
import org.junit.Test;



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
        Assert.assertTrue("color has 7 digits", colorValidator.isValid("#123456",null));
        Assert.assertFalse("color has 7 digits", colorValidator.isValid("#1234567",null));
        Assert.assertFalse("color has 7 digits", colorValidator.isValid("#12345",null));
    }
    
    /**
     * #
     */
    @Test
    public void colorMustStartWithDiez(){
        ColorValidator colorValidator = new ColorValidator();
        Assert.assertTrue("color must start with #", colorValidator.isValid("#123456",null));
        Assert.assertFalse("color must start with #", colorValidator.isValid("1234567",null));        
    }
    
    /**
     * hex
     */
    @Test
    public void onlyHexaDigitsPermited(){
        ColorValidator colorValidator = new ColorValidator();
        Assert.assertTrue("color must start with #", colorValidator.isValid("#123456",null));
        Assert.assertTrue("color must start with #", colorValidator.isValid("#7890AB",null));    
        Assert.assertTrue("color must start with #", colorValidator.isValid("#CDEFab",null));    
        Assert.assertTrue("color must start with #", colorValidator.isValid("#cdef01",null));    
        
        Assert.assertFalse("color must start with #", colorValidator.isValid("#Gdef01",null));
    }
    
    /**
     *  null color
     */
    @Test
    public void nullColorIsValid(){
        ColorValidator colorValidator = new ColorValidator();
        Assert.assertTrue("null color is valid", colorValidator.isValid(null,null));
        Assert.assertTrue("empty color is valid", colorValidator.isValid("",null));
    }
    
}
