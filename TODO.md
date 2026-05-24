1. Better repr for nested objects in csv
2. markdown table repr
3. ascii table repr
4. only show select fields (allowing nested) in any repr (e.g.: drop irrelevant / noise fields; but fail loudly if they don't exist)
5. add way to pre-build snap object with config. I.e.: builder or similar that when built is ready to accept an object to compare with snapshot per pre-configuration; useful for configuring behaviour in test class for all tests