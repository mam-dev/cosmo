package org.unitedinternet.cosmo.hibernate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class ColorValidator implements ConstraintValidator<Color, String> {
    private char[] validChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F'};


    @Override
    public void initialize(Color constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        /*maybe regex :
         * Pattern p = Pattern.compile("^#([A-Fa-f0-9]{6}");
         * Mather m = p.matcher(value);
         * return m.find(); ?
         */
        if (value.equals("")) {
            return true;
        }
        if (value.length() != 7) {
            return false;
        }
        if (!value.startsWith("#")) {
            return false;
        }
        for (int i = 1; i < 7; i++) {
            if (!isValidChar(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidChar(char charAt) {
        for (char c : validChars) {
            if (charAt == c) {
                return true;
            }
        }
        return false;
    }

}
