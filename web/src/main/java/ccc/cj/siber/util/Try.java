package ccc.cj.siber.util;

import ccc.cj.siber.database.model.Result;
import ccc.cj.siber.web.controller.CompareController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author chenjiong
 * @date 26/02/2018 17:52
 */
public class Try {
    private static final Logger logger = LoggerFactory.getLogger(CompareController.class);

    public static Result supplier(UncheckedSupplier<Result> mapper) {
        try {
            return mapper.get();
        } catch (Throwable t) {
            logger.error("", t);
            return Result.fail(Constant.INTERNAL_ERROR);
        }
    }

    public static <T, R> Function<T, R> function(UncheckedFunction<T, R> mapper) {
        Objects.requireNonNull(mapper);
        return t -> {
            try {
                return mapper.apply(t);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public static <T> Consumer<T> consumer(UncheckedConsumer<T> mapper) {
        Objects.requireNonNull(mapper);
        return t -> {
            try {
                mapper.apply(t);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    @FunctionalInterface
    public interface UncheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    @FunctionalInterface
    public interface UncheckedConsumer<T> {
        void apply(T t) throws Exception;
    }

    @FunctionalInterface
    public interface UncheckedSupplier<T> {
        T get() throws Exception;
    }


}
