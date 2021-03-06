package com.raizlabs.android.dbflow.structure;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;

/**
 * Author: andrewgrosner
 * Description: Internal adapter that gets extended when a {@link com.raizlabs.android.dbflow.annotation.Table} gets used.
 */
public abstract class ModelAdapter<ModelClass extends Model>
        implements InternalAdapter<ModelClass, ModelClass>, InstanceAdapter<ModelClass, ModelClass> {

    private ConditionQueryBuilder<ModelClass> mPrimaryWhere;

    private SQLiteStatement mInsertStatement;

    /**
     * @return The precompiled insert statement for this table model adapter
     */
    public SQLiteStatement getInsertStatement() {
        if (mInsertStatement == null) {
            mInsertStatement = FlowManager.getDatabaseForTable(getModelClass())
                    .getWritableDatabase().compileStatement(getInsertStatementQuery());
        }

        return mInsertStatement;
    }

    /**
     * Creates a new {@link ModelClass} and Loads the cursor into a the object.
     *
     * @param cursor The cursor to load
     * @return A new {@link ModelClass}
     */
    public ModelClass loadFromCursor(Cursor cursor) {
        ModelClass model = newInstance();
        loadFromCursor(cursor, model);
        return model;
    }

    /**
     * Saves the specified model to the DB using the specified saveMode in {@link com.raizlabs.android.dbflow.sql.SqlUtils}.
     *
     * @param model The model to save/insert/update
     */
    @Override
    public void save(ModelClass model) {
        SqlUtils.save(model, this, this);
    }

    /**
     * Inserts the specified model into the DB.
     *
     * @param model The model to insert.
     */
    @Override
    public void insert(ModelClass model) {
        SqlUtils.insert(model, this, this);
    }

    /**
     * Updates the specified model into the DB.
     *
     * @param model The model to update.
     */
    @Override
    public void update(ModelClass model) {
        SqlUtils.update(model, this, this);
    }

    /**
     * Deletes the model from the DB
     *
     * @param model The model to delete
     */
    @Override
    public void delete(ModelClass model) {
        SqlUtils.delete(model, this);
    }

    /**
     * If a {@link com.raizlabs.android.dbflow.structure.Model} has an autoincrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    @Override
    public void updateAutoIncrement(ModelClass model, long id) {

    }

    /**
     * @return The value for the {@link PrimaryKey#autoincrement()}
     * if it has the field. This method is overridden when its specified for the {@link ModelClass}
     */
    @Override
    public long getAutoIncrementingId(ModelClass model) {
        throw new InvalidDBConfiguration(
                String.format("This method may have been called in error. The model class %1s must contain" +
                              "a single primary key (if used in a ModelCache, this method may be called)",
                              getModelClass()));
    }

    /**
     * @return The autoincrement column name for the {@link PrimaryKey#autoincrement()}
     * if it has the field. This method is overridden when its specified for the {@link ModelClass}
     */
    public String getAutoIncrementingColumnName() {
        throw new InvalidDBConfiguration(
                String.format("This method may have been called in error. The model class %1s must contain" +
                              "an autoincrementing or single int/long primary key (if used in a ModelCache, this method may be called)",
                              getModelClass()));
    }

    /**
     * @param model The model to retrieve the caching id from.
     * @return The id that comes from the model. This is generated by subclasses of this adapter.
     */
    @Override
    public Object getCachingId(ModelClass model) {
        return getAutoIncrementingId(model);
    }

    /**
     * @return The name of the column used in caching. By default it uses the auto increment column,
     * when specified in the {@link com.raizlabs.android.dbflow.annotation.Table}, it is overridden.
     */
    public String getCachingColumnName() {
        return getAutoIncrementingColumnName();
    }

    /**
     * @param cursor      The cursor to retrieve data from.
     * @param columnIndex The column index to retrieve data from.
     * @return The cache id value from the {@link Cursor}. This is generated since not all
     * columns have the same value.
     */
    public Object getCachingIdFromCursorIndex(Cursor cursor, int columnIndex) {
        return cursor.getLong(columnIndex);
    }

    /**
     * @return Only created once if doesn't exist, the extended class will return the builder to use.
     */
    protected abstract ConditionQueryBuilder<ModelClass> createPrimaryModelWhere();

    /**
     * Will create the where query only once that is used to check for existence in the DB.
     *
     * @return The WHERE query containing all primary key fields
     */
    public ConditionQueryBuilder<ModelClass> getPrimaryModelWhere() {
        if (mPrimaryWhere == null) {
            mPrimaryWhere = createPrimaryModelWhere();
        }
        mPrimaryWhere.setUseEmptyParams(true);
        return mPrimaryWhere;
    }

    /**
     * @return The query used to create this table.
     */
    public abstract String getCreationQuery();

    /**
     * @return The query used to insert a model using a {@link android.database.sqlite.SQLiteStatement}
     */
    protected abstract String getInsertStatementQuery();

    /**
     * @return The conflict algorithm to use when updating a row in this table.
     */
    public ConflictAction getUpdateOnConflictAction() {
        return ConflictAction.ABORT;
    }

    /**
     * @return The conflict algorithm to use when inserting a row in this table.
     */
    public ConflictAction getInsertOnConflictAction() {
        return ConflictAction.ABORT;
    }
}
