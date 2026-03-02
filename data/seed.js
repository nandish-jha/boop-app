// Seed catalogs used by the app
window.SEED = {
  habits: [
    { id: 'h_sleep', name: 'Sleep consistency', unit: 'hrs', target: 8, type: 'quant' },
    { id: 'h_water', name: 'Water intake', unit: 'cups', target: 8, type: 'quant' },
    { id: 'h_read',  name: 'Reading',         unit: 'min', target: 30, type: 'quant' },
    { id: 'h_medit', name: 'Meditation',      unit: 'min', target: 10, type: 'quant' },
    { id: 'h_pray',  name: 'Pray to god',     unit: 'times', target: 1, type: 'quant' },
    { id: 'h_food',  name: 'No outside food', unit: '',    target: 1, type: 'check' },
    { id: 'h_gym',   name: 'Gym',             unit: '',    target: 1, type: 'check' },
    { id: 'h_wake',  name: '4am wake up',     unit: '',    target: 1, type: 'check' },
    { id: 'h_grat',  name: 'Gratitude (night)', unit: '',  target: 1, type: 'check' }
  ],
  supplements: [
    { id: 's1', name: 'Vitamin D3', time: 'morning', dose: '1000 IU' },
    { id: 's2', name: 'Omega 3 Fish Oil', time: 'morning', dose: '1 cap (with food)' },
    { id: 's3', name: 'Zinc', time: 'morning', dose: '15 mg' },
    { id: 's4', name: 'Rhodiola Rosea', time: 'morning', dose: '300 mg' },
    { id: 's5', name: 'Probiotic', time: 'morning', dose: '1 cap' },
    { id: 's6', name: 'Whey Protein Isolate', time: 'workout', dose: '1 scoop (after)' },
    { id: 's7', name: 'Electrolytes', time: 'workout', dose: '1 serving' },
    { id: 's8', name: 'Creatine', time: 'workout', dose: '5 g' },
    { id: 's9', name: 'L-Citrulline', time: 'workout', dose: '6 g' },
    { id: 's10', name: 'Magnesium Glycinate', time: 'night', dose: '400 mg' },
    { id: 's11', name: 'Ashwagandha', time: 'night', dose: '600 mg' },
    { id: 's12', name: 'Glycine', time: 'night', dose: '3 g' },
    { id: 's13', name: 'L-Theanine', time: 'night', dose: '200 mg' },
    { id: 's14', name: 'Inositol', time: 'night', dose: '2 g' }
  ],
  skincare: [
    { id: 'sk1', name: 'Gel cleanser', time: 'morning' },
    { id: 'sk2', name: 'Moisturizer', time: 'morning' },
    { id: 'sk3', name: 'Sunscreen', time: 'morning' },
    { id: 'sk4', name: 'Facewash', time: 'night' },
    { id: 'sk5', name: 'Moisturizer', time: 'night' },
    { id: 'sk6', name: 'Under-eye serum / gel strips (optional)', time: 'night' }
  ],
  budgetCategories: [
    'Hanging out with friends', 'Shopping', 'Phone bill',
    'Gym subscription', 'Perplexity AI subscription',
    'Groceries', 'Food', 'Transport', 'Rent', 'Utilities',
    'Transfer', 'Investment', 'Other'
  ],
  accounts: [
    { id: 'a1', name: 'Scotiabank Chequing', type: 'chequing', bank: 'Scotiabank' },
    { id: 'a2', name: 'Scotiabank Savings', type: 'savings', bank: 'Scotiabank' },
    { id: 'a3', name: 'Scotiabank TFSA', type: 'tfsa', bank: 'Scotiabank' },
    { id: 'a4', name: 'Scotiabank Credit Card', type: 'credit', bank: 'Scotiabank' },
    { id: 'a5', name: 'TD Chequing', type: 'chequing', bank: 'TD' },
    { id: 'a6', name: 'TD Savings', type: 'savings', bank: 'TD' },
    { id: 'a7', name: 'TD TFSA', type: 'tfsa', bank: 'TD' },
    { id: 'a8', name: 'TD Credit Card', type: 'credit', bank: 'TD' },
    { id: 'a9', name: 'Affinity Chequing', type: 'chequing', bank: 'Affinity' },
    { id: 'a10', name: 'Affinity Savings', type: 'savings', bank: 'Affinity' },
    { id: 'a11', name: 'Affinity TFSA', type: 'tfsa', bank: 'Affinity' },
    { id: 'a12', name: 'Wealthsimple Chequing', type: 'chequing', bank: 'Wealthsimple' },
    { id: 'a13', name: 'Wealthsimple TFSA', type: 'tfsa', bank: 'Wealthsimple' },
    { id: 'a14', name: 'Loblaw EOSP', type: 'equity', bank: 'Loblaw' },
    { id: 'a15', name: 'University Loan', type: 'loan', bank: 'Gov' }
  ],
  workouts: []
};
