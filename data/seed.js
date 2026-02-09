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
  budgetCategories: [],
  accounts: [],
  workouts: []
};
