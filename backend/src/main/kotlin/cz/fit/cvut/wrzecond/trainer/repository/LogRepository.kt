package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Log
import org.springframework.stereotype.Repository
import java.sql.Timestamp

/**
 * Repository interface for managing Logs entities.
 */
@Repository
interface LogRepository: IRepository<Log> {
    /**
     * Deletes all log entries where the timestamp is less than the specified date.
     *
     * @param date The timestamp used as the threshold. Log entries with timestamps earlier than this date will be deleted.
     */
    fun deleteAllByTimestampIsLessThan(date: Timestamp)
}