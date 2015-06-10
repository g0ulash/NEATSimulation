%% Calculate mean of each experiment, put in vector
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

%% plot average crossover
clear all;
close all;
nr_exps = 30;
N = 3001;

crossovers_static = zeros(nr_exps, N);
crossovers_dynamic = zeros(nr_exps, N);

for exps = 0:(nr_exps-1)
    FILENAME = ['experiment_' num2str(exps) '_dynamic_false.csv'];
    %save each row of crossover
    a = csvread( FILENAME, 1, 0, [0 9 N-1 9] );
    a = a(:,2);
    crossovers_static(exps+1, :) = a;
    clearvars a
    %load the dynamic results
    FILENAME = ['experiment_' num2str(exps) '_dynamic_true.csv'];
    %save each row of crossover
    a = csvread( FILENAME, 1, 0, [0 9 N-1 9] );
    a = a(:,2);
    crossovers_dynamic(exps+1, :) = a;
    clearvars a
end

average_crossovers_static = mean(crossovers_static,1);
average_crossovers_dynamic = mean(crossovers_dynamic,1);

x = 0:100:300000;

plot(x,average_crossovers_static, '-r', x, average_crossovers_dynamic, '-b');
legend('Static', 'Dynamic');
ylabel('Average Crossover');
xlabel('Iteration');

%% plot average fitness
clear all;
close all;
nr_exps = 30;
N = 3001;

fitness_static = zeros(nr_exps, N);
fitness_dynamic = zeros(nr_exps, N);

for exps = 0:(nr_exps-1)
    FILENAME = ['experiment_' num2str(exps) '_dynamic_false.csv'];
    %save each row of crossover
    a = csvread( FILENAME, 1, 0, [0 6 N-1 6] );
    a = a(:,2);
    fitness_static(exps+1, :) = a;
    clearvars a
    %load the dynamic results
    FILENAME = ['experiment_' num2str(exps) '_dynamic_true.csv'];
    %save each row of crossover
    a = csvread( FILENAME, 1, 0, [0 6 N-1 6] );
    a = a(:,2);
    fitness_dynamic(exps+1, :) = a;
    clearvars a
end

average_fitness_static = mean(fitness_static,1);
average_fitness_dynamic = mean(fitness_dynamic,1);

x = 0:100:300000;

plot(x,average_fitness_static, '-r', x, average_fitness_dynamic, '-b');
legend('Static', 'Dynamic');
ylabel('Average Fitness');
xlabel('Iteration');

%% plot average food and poison age
clear all;
close all;
nr_exps = 30;
N = 3001;

food_age_static = zeros(nr_exps, N);
poison_age_static = zeros(nr_exps, N);
food_age_dynamic = zeros(nr_exps, N);
poison_age_dynamic = zeros(nr_exps, N);

for exps = 0:(nr_exps-1)
    FILENAME = ['experiment_' num2str(exps) '_dynamic_false.csv'];
    %save each row of crossover
    a = csvread( FILENAME, 1, 0, [0 3 N-1 4] );
    b = a(:,2);
    a = a(:,3);
    food_age_static(exps+1, :) = b;
    poison_age_static(exps+1, :) = a;
    clearvars a b
    %load the dynamic results
    FILENAME = ['experiment_' num2str(exps) '_dynamic_true.csv'];
    %save each row of crossover
    a = csvread( FILENAME, 1, 0, [0 3 N-1 4] );
    b = a(:,2);
    a = a(:,3);
    food_age_dynamic(exps+1, :) = b;
    poison_age_dynamic(exps+1, :) = a;
    clearvars a b
end

average_food_age_static = mean(food_age_static,1);
average_poison_age_static = mean(poison_age_static,1);
average_food_age_dynamic = mean(food_age_dynamic,1);
average_poison_age_dynamic = mean(poison_age_dynamic,1);

x = 0:100:300000;

plot(x,average_food_age_static, '-r', x, average_food_age_dynamic, '-b');
legend('Static', 'Dynamic');
ylabel('Average Food Age');
xlabel('Iteration');
figure;
plot(x,average_poison_age_static, '-r', x, average_poison_age_dynamic, '-b');
legend('Static', 'Dynamic');
ylabel('Average Poison Age');
xlabel('Iteration');