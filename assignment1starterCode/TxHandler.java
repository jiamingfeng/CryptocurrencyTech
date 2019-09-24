import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        publicLedger = utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        
        // (1) all outputs claimed by {@code tx} are in the current UTXO pool

        // (2) the signatures on each input of {@code tx} are valid
        ArrayList<Transaction.Input> txInputs = tx.getInputs();
        int inputIndex = 0;

        // to guarantee the uniqueness of the transaction inputs
        Set<UTXO> setUTXO = new HashSet<>();
        
        // sum values difference between transaction inputs and outputs
        double differenceBetweenTx = 0.0;

        for (Transaction.Input input : txInputs) {
            UTXO prevTXO = new UTXO(input.prevTxHash, input.outputIndex);
            if ( !publicLedger.contains(prevTXO) ) {
                return false;
            }

            Transaction.Output prevOutput = publicLedger.getTxOutput(prevTXO);
            if ( ! Crypto.verifySignature(prevOutput.address, tx.getRawDataToSign(inputIndex), input.signature) ) {
                return false;
            }

            // (3) no UTXO is claimed multiple times by {@code tx}
            if ( setUTXO.contains(prevTXO) )
                return false;

            setUTXO.add(prevTXO);

            differenceBetweenTx += prevOutput.value;

            ++inputIndex;
        }

        // (4) all of {@code tx}s output values are non-negative
        ArrayList<Transaction.Output> txOutputs = tx.getOutputs();
        for (Transaction.Output output : txOutputs) {
            if ( output.value < 0 )
                return false;

            differenceBetweenTx -= output.value;
        }

        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values
        return ( differenceBetweenTx >= 0.0 );
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        
        Transaction[] result = {};
        ArrayList<Transaction> validTransactions = new ArrayList<>();
        for( Transaction tx : possibleTxs )
        {
            if ( isValidTx(tx) )
            {                
                // update the current UTXO pool by removing transaction inputs as UTXO and adding outputs as UTXO
                ArrayList<Transaction.Input> txInputs = tx.getInputs();
                for (Transaction.Input input : txInputs) {
                    UTXO oldTXO = new UTXO(input.prevTxHash, input.outputIndex);
                    publicLedger.removeUTXO(oldTXO);
                }            
                ArrayList<Transaction.Output> txOutputs = tx.getOutputs();
                int outputIndex = 0;
                for (Transaction.Output output : txOutputs) {
                    UTXO newTXO = new UTXO(tx.getHash(), outputIndex);
                    publicLedger.addUTXO(newTXO, output);
                    ++outputIndex;
                }

                validTransactions.add(tx);
            }
        }

        // return an empty array if no transaction is valid
        if ( validTransactions.isEmpty() )
            return result;

        result = new Transaction[validTransactions.size()]; 
        result = validTransactions.toArray(result);
        return result;
    }

    protected UTXOPool publicLedger;

}
