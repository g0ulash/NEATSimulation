nr_exps = 30;

data_static = zeros(nr_exps,1);
data_dynamic = zeros(nr_exps,1);

N = 3001; %number of rows in the file

for exps = 0:(nr_exps-1)
   %load the static results
   FILENAME = ['experiment_' num2str(exps) '_dynamic_false.csv'];
   %save the static results
   a = csvread( FILENAME, 1, 0, [0 9 N-1 9 ] );
   a = a(:,2);
   data_static(exps+1) = mean(a);
   clearvars -except data_static data_dynamic exps N
   %load the dynamic results
   FILENAME = (['experiment_' num2str(exps) '_dynamic_true.csv']);
   %save the dynamic results
   a = csvread( FILENAME, 1, 0, [0 9 N-1 9 ] );
   a = a(:,2);
   data_dynamic(exps+1) = mean(a);
   clearvars -except data_static data_dynamic exps N
end