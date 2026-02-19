package com.example.officetracker.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.officetracker.data.local.entity.AttendanceSession;
import com.example.officetracker.data.local.entity.DailyStat;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AttendanceDao_Impl implements AttendanceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AttendanceSession> __insertionAdapterOfAttendanceSession;

  private final EntityInsertionAdapter<DailyStat> __insertionAdapterOfDailyStat;

  private final EntityDeletionOrUpdateAdapter<AttendanceSession> __deletionAdapterOfAttendanceSession;

  private final EntityDeletionOrUpdateAdapter<AttendanceSession> __updateAdapterOfAttendanceSession;

  public AttendanceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAttendanceSession = new EntityInsertionAdapter<AttendanceSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `attendance_sessions` (`id`,`date`,`startTime`,`endTime`,`isManual`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AttendanceSession entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getDate());
        statement.bindLong(3, entity.getStartTime());
        if (entity.getEndTime() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getEndTime());
        }
        final int _tmp = entity.isManual() ? 1 : 0;
        statement.bindLong(5, _tmp);
      }
    };
    this.__insertionAdapterOfDailyStat = new EntityInsertionAdapter<DailyStat>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `daily_stats` (`date`,`totalSeconds`,`cappedSeconds`,`isGoalMet`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DailyStat entity) {
        statement.bindLong(1, entity.getDate());
        statement.bindLong(2, entity.getTotalSeconds());
        statement.bindLong(3, entity.getCappedSeconds());
        final int _tmp = entity.isGoalMet() ? 1 : 0;
        statement.bindLong(4, _tmp);
      }
    };
    this.__deletionAdapterOfAttendanceSession = new EntityDeletionOrUpdateAdapter<AttendanceSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `attendance_sessions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AttendanceSession entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfAttendanceSession = new EntityDeletionOrUpdateAdapter<AttendanceSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `attendance_sessions` SET `id` = ?,`date` = ?,`startTime` = ?,`endTime` = ?,`isManual` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AttendanceSession entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getDate());
        statement.bindLong(3, entity.getStartTime());
        if (entity.getEndTime() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getEndTime());
        }
        final int _tmp = entity.isManual() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getId());
      }
    };
  }

  @Override
  public Object insertSession(final AttendanceSession session,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAttendanceSession.insertAndReturnId(session);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertOrUpdateDailyStat(final DailyStat stat,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDailyStat.insert(stat);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSession(final AttendanceSession session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAttendanceSession.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSession(final AttendanceSession session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAttendanceSession.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCurrentActiveSession(final Continuation<? super AttendanceSession> $completion) {
    final String _sql = "SELECT * FROM attendance_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AttendanceSession>() {
      @Override
      @Nullable
      public AttendanceSession call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfIsManual = CursorUtil.getColumnIndexOrThrow(_cursor, "isManual");
          final AttendanceSession _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final boolean _tmpIsManual;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManual);
            _tmpIsManual = _tmp != 0;
            _result = new AttendanceSession(_tmpId,_tmpDate,_tmpStartTime,_tmpEndTime,_tmpIsManual);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AttendanceSession>> getSessionsForDate(final long date) {
    final String _sql = "SELECT * FROM attendance_sessions WHERE date = ? ORDER BY startTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"attendance_sessions"}, new Callable<List<AttendanceSession>>() {
      @Override
      @NonNull
      public List<AttendanceSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfIsManual = CursorUtil.getColumnIndexOrThrow(_cursor, "isManual");
          final List<AttendanceSession> _result = new ArrayList<AttendanceSession>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AttendanceSession _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final boolean _tmpIsManual;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManual);
            _tmpIsManual = _tmp != 0;
            _item = new AttendanceSession(_tmpId,_tmpDate,_tmpStartTime,_tmpEndTime,_tmpIsManual);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSessionsForDateSync(final long date,
      final Continuation<? super List<AttendanceSession>> $completion) {
    final String _sql = "SELECT * FROM attendance_sessions WHERE date = ? ORDER BY startTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AttendanceSession>>() {
      @Override
      @NonNull
      public List<AttendanceSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfIsManual = CursorUtil.getColumnIndexOrThrow(_cursor, "isManual");
          final List<AttendanceSession> _result = new ArrayList<AttendanceSession>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AttendanceSession _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final boolean _tmpIsManual;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManual);
            _tmpIsManual = _tmp != 0;
            _item = new AttendanceSession(_tmpId,_tmpDate,_tmpStartTime,_tmpEndTime,_tmpIsManual);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AttendanceSession>> getAllSessions() {
    final String _sql = "SELECT * FROM attendance_sessions ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"attendance_sessions"}, new Callable<List<AttendanceSession>>() {
      @Override
      @NonNull
      public List<AttendanceSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfIsManual = CursorUtil.getColumnIndexOrThrow(_cursor, "isManual");
          final List<AttendanceSession> _result = new ArrayList<AttendanceSession>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AttendanceSession _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final boolean _tmpIsManual;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManual);
            _tmpIsManual = _tmp != 0;
            _item = new AttendanceSession(_tmpId,_tmpDate,_tmpStartTime,_tmpEndTime,_tmpIsManual);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<DailyStat> getDailyStat(final long date) {
    final String _sql = "SELECT * FROM daily_stats WHERE date = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"daily_stats"}, new Callable<DailyStat>() {
      @Override
      @Nullable
      public DailyStat call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfTotalSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSeconds");
          final int _cursorIndexOfCappedSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "cappedSeconds");
          final int _cursorIndexOfIsGoalMet = CursorUtil.getColumnIndexOrThrow(_cursor, "isGoalMet");
          final DailyStat _result;
          if (_cursor.moveToFirst()) {
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpTotalSeconds;
            _tmpTotalSeconds = _cursor.getLong(_cursorIndexOfTotalSeconds);
            final long _tmpCappedSeconds;
            _tmpCappedSeconds = _cursor.getLong(_cursorIndexOfCappedSeconds);
            final boolean _tmpIsGoalMet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsGoalMet);
            _tmpIsGoalMet = _tmp != 0;
            _result = new DailyStat(_tmpDate,_tmpTotalSeconds,_tmpCappedSeconds,_tmpIsGoalMet);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<DailyStat>> getStatsRange(final long startDate, final long endDate) {
    final String _sql = "SELECT * FROM daily_stats WHERE date >= ? AND date <= ? ORDER BY date ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"daily_stats"}, new Callable<List<DailyStat>>() {
      @Override
      @NonNull
      public List<DailyStat> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfTotalSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSeconds");
          final int _cursorIndexOfCappedSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "cappedSeconds");
          final int _cursorIndexOfIsGoalMet = CursorUtil.getColumnIndexOrThrow(_cursor, "isGoalMet");
          final List<DailyStat> _result = new ArrayList<DailyStat>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyStat _item;
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpTotalSeconds;
            _tmpTotalSeconds = _cursor.getLong(_cursorIndexOfTotalSeconds);
            final long _tmpCappedSeconds;
            _tmpCappedSeconds = _cursor.getLong(_cursorIndexOfCappedSeconds);
            final boolean _tmpIsGoalMet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsGoalMet);
            _tmpIsGoalMet = _tmp != 0;
            _item = new DailyStat(_tmpDate,_tmpTotalSeconds,_tmpCappedSeconds,_tmpIsGoalMet);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Long> getTotalCappedSecondsRange(final long startDate, final long endDate) {
    final String _sql = "SELECT SUM(cappedSeconds) FROM daily_stats WHERE date >= ? AND date <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"daily_stats"}, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<DailyStat>> getAllDailyStats() {
    final String _sql = "SELECT * FROM daily_stats ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"daily_stats"}, new Callable<List<DailyStat>>() {
      @Override
      @NonNull
      public List<DailyStat> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfTotalSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSeconds");
          final int _cursorIndexOfCappedSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "cappedSeconds");
          final int _cursorIndexOfIsGoalMet = CursorUtil.getColumnIndexOrThrow(_cursor, "isGoalMet");
          final List<DailyStat> _result = new ArrayList<DailyStat>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyStat _item;
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpTotalSeconds;
            _tmpTotalSeconds = _cursor.getLong(_cursorIndexOfTotalSeconds);
            final long _tmpCappedSeconds;
            _tmpCappedSeconds = _cursor.getLong(_cursorIndexOfCappedSeconds);
            final boolean _tmpIsGoalMet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsGoalMet);
            _tmpIsGoalMet = _tmp != 0;
            _item = new DailyStat(_tmpDate,_tmpTotalSeconds,_tmpCappedSeconds,_tmpIsGoalMet);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
