
public class MaxFeeTxHandler extends TxHandler {

    public MaxFeeTxHandler(UTXOPool utxoPool)
    {
        super(utxoPool);
    }

    @Override
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        Transaction[] validTransactions = super.handleTxs(possibleTxs);

        return validTransactions;
    }
}