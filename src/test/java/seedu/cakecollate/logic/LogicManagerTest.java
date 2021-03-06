package seedu.cakecollate.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.cakecollate.commons.core.Messages.MESSAGE_INVALID_ORDER_DISPLAYED_INDEX;
import static seedu.cakecollate.commons.core.Messages.MESSAGE_ORDERS_LISTED_OVERVIEW;
import static seedu.cakecollate.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.cakecollate.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.cakecollate.logic.commands.CommandTestUtil.DELIVERY_DATE_DESC_AMY;
import static seedu.cakecollate.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.cakecollate.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.cakecollate.logic.commands.CommandTestUtil.ORDER_DESC_AMY;
import static seedu.cakecollate.logic.commands.CommandTestUtil.ORDER_ITEM_AMY;
import static seedu.cakecollate.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.cakecollate.testutil.Assert.assertThrows;
import static seedu.cakecollate.testutil.TypicalOrders.AMY;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.cakecollate.logic.commands.AddCommand;
import seedu.cakecollate.logic.commands.CommandResult;
import seedu.cakecollate.logic.commands.ListCommand;
import seedu.cakecollate.logic.commands.exceptions.CommandException;
import seedu.cakecollate.logic.parser.exceptions.ParseException;
import seedu.cakecollate.model.Model;
import seedu.cakecollate.model.ModelManager;
import seedu.cakecollate.model.ReadOnlyCakeCollate;
import seedu.cakecollate.model.UserPrefs;
import seedu.cakecollate.model.order.Order;
import seedu.cakecollate.storage.JsonCakeCollateStorage;
import seedu.cakecollate.storage.JsonOrderItemsStorage;
import seedu.cakecollate.storage.JsonUserPrefsStorage;
import seedu.cakecollate.storage.StorageManager;
import seedu.cakecollate.testutil.OrderBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonCakeCollateStorage cakeCollateStorage =
                new JsonCakeCollateStorage(temporaryFolder.resolve("cakeCollate.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        JsonOrderItemsStorage orderItemsStorage = new JsonOrderItemsStorage(temporaryFolder.resolve("OrderItems.json"));
        StorageManager storage = new StorageManager(cakeCollateStorage, userPrefsStorage, orderItemsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_ORDER_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        String expectedMessage = String.format(MESSAGE_ORDERS_LISTED_OVERVIEW, model.getFilteredOrderList().size());
        assertCommandSuccess(listCommand, expectedMessage, model);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        // Setup LogicManager with JsonCakeCollateIoExceptionThrowingStub
        JsonCakeCollateStorage cakeCollateStorage =
                new JsonCakeCollateIoExceptionThrowingStub(temporaryFolder.resolve("ioExceptionCakeCollate.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ioExceptionUserPrefs.json"));
        JsonOrderItemsStorage orderItemsStorage = new JsonOrderItemsStorage(temporaryFolder.resolve("OrderItems.json"));
        StorageManager storage = new StorageManager(cakeCollateStorage, userPrefsStorage, orderItemsStorage);
        logic = new LogicManager(model, storage);

        // Execute add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + ADDRESS_DESC_AMY + ORDER_DESC_AMY + DELIVERY_DATE_DESC_AMY;
        Order expectedOrder = new OrderBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addOrder(expectedOrder);
        expectedModel.addOrderItem(ORDER_ITEM_AMY);
        String expectedMessage = LogicManager.FILE_OPS_ERROR_MESSAGE + DUMMY_IO_EXCEPTION;
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    @Test
    public void getFilteredOrderList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredOrderList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getCakeCollate(), new UserPrefs(), model.getOrderItems());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * A stub class to throw an {@code IOException} when the save method is called.
     */
    private static class JsonCakeCollateIoExceptionThrowingStub extends JsonCakeCollateStorage {
        private JsonCakeCollateIoExceptionThrowingStub(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveCakeCollate(ReadOnlyCakeCollate cakeCollate, Path filePath) throws IOException {
            throw DUMMY_IO_EXCEPTION;
        }
    }
}
