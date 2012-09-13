package org.books.presentation.validation.jsf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.books.common.data.CreditCard;
import org.books.presentation.MessageFactory;

/**
 *
 * @author Christoph Horber
 */
@FacesValidator("org.books.CreditCardValidator")
public class CreditCardValidator implements Validator, StateHolder {

    public static final String INVALID_FORMAT_MESSAGE_ID = "org.books.CreditCardValidator.INVALID_FORMAT";
    
    public static final String INVALID_NUMBER_MESSAGE_ID = "org.books.CreditCardValidator.INVALID_NUMBER";
    
    private static final Pattern PATTERN = Pattern.compile("([0-9]{4} ?){4}");
    
    private static final String EXAMPLE = "1111 2222 3333 4444";
    
    private String cardTypeId;
    
    private boolean transientValue = false;
    
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {        
        UIInput typeComponent = (UIInput) component.findComponent(cardTypeId);
        CreditCard.Type type = (CreditCard.Type) typeComponent.getValue();
        if (type != null) {

            String cardNumber = ((String) value).trim();
            Matcher matcher = PATTERN.matcher(cardNumber);
            if (!matcher.matches()) {
                throw new ValidatorException(MessageFactory.getMessage(FacesMessage.SEVERITY_ERROR, INVALID_FORMAT_MESSAGE_ID, cardNumber, EXAMPLE));
            }
            cardNumber = cardNumber.replaceAll(" ", "");
            if (!check(cardNumber)) {
                throw new ValidatorException(MessageFactory.getMessage(FacesMessage.SEVERITY_ERROR, INVALID_NUMBER_MESSAGE_ID, cardNumber));
            }

            if (!isNumberValidForType(type, cardNumber)) {
                throw new ValidatorException(MessageFactory.getMessage(FacesMessage.SEVERITY_ERROR, INVALID_NUMBER_MESSAGE_ID, cardNumber));
            }
        }
    }
    
    /**
     * LUHN algorithm
     */
    private static boolean check(String number) {
        int len = number.length();
        int sum = 0;
        for (int i = 1; i <= len; i++) {
                int digit = Character.digit(number.charAt(len - i), 10);
                if (i % 2 == 0) {
                        digit = 2 * digit;
                }
                if (digit > 9) {
                        digit = digit - 9;
                }
                sum = sum + digit;
        }
        
        return sum % 10 == 0;
    }

    private boolean isNumberValidForType(CreditCard.Type type, String cardNumber) {
        // type can not be null
        switch (type) {
            case MasterCard:
                int digits = Integer.parseInt(cardNumber.substring(0, 2));
                return digits >= 51 && digits <= 55;
            case Visa:
                return cardNumber.charAt(0) == '4';
            default:
                return false;
        } 
    }

    @Override
    public Object saveState(FacesContext context) {
        return cardTypeId;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        cardTypeId = (String) state;
    }

    @Override
    public boolean isTransient() {
        return this.transientValue;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
        this.transientValue = newTransientValue;
    }

    /**
     * @return the cardTypeId
     */
    public String getCardTypeId() {
        return cardTypeId;
    }

    /**
     * @param cardTypeId the cardTypeId to set
     */
    public void setCardTypeId(String cardTypeId) {
        this.cardTypeId = cardTypeId;
    }
}
