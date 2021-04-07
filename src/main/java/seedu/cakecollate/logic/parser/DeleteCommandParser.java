package seedu.cakecollate.logic.parser;

import static seedu.cakecollate.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.cakecollate.commons.core.Messages;
import seedu.cakecollate.commons.core.index.IndexList;
import seedu.cakecollate.logic.commands.DeleteCommand;
import seedu.cakecollate.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        boolean allDigitsAndLengthMoreThanTen = false;
        try {
            allDigitsAndLengthMoreThanTen = args.trim().chars().allMatch(Character::isDigit)
                    && args.length() > ParserUtil.INTEGER_LENGTH;
            IndexList indexList = ParserUtil.parseIndexList(args);
            return new DeleteCommand(indexList);
        } catch (ParseException pe) {
            if (allDigitsAndLengthMoreThanTen) {
                throw new ParseException(Messages.MESSAGE_INVALID_ORDER_DISPLAYED_INDEX);
            }
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), pe);
        }
    }
}
