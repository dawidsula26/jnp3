# Disclaimer

Prawdopodobnie przesadziłem ze scopem naszej aplikacji, ale wolę zaprezentować więcej, a potem wybierać to, co jest ważne. 

# Wizja

Naszym podstawowym celem jest stworzenie aplikacji umożliwiającej analizę danych w czasie rzeczywistym i wyświetlanie wyników na FE w postaci wykresów `Time -> Value`, liczników z aktualnymi wartościami wskaźników, lub innych sposobów, które uznamy za odpowiednie. Przypadkiem użycia, na którym będziemy się skupiać, jest analiza cen na giełdzie, ale chcemy, aby aplikacje dawało się łatwo stosować także do innych sytuacji. 

Jednym z celów aplikacji jest umożliwienie łatwego tworzenia i modyfikacji istniejących reguł wykorzystywanych do analizy danych. Powinno dać się zmienić co i jak jest wyliczane bez modyfikowania kodu żadnego segmentu aplikacji poza mikroserwisem odpowiedzialnym za liczenie tego. Dodatkowo implementacja mikroserwisu przeliczającego to nie powinna wymagać przejmowania się problemami normalnie występującymi przy obliczeniach rozproszonych, takimi jak utrzymywanie stanu, zabezpieczeniem przed błędami, ... .

# Opis

## Pojęcia 

***Zmienna*** &ndash; wartość, która zmienia się w czasie; może być reprezentowana jako funkcja `f: Time -> Value`; może to być cena akcji, liczba wejść na stronę, odchylenie standardowe innej zmiennej, ... .

***Użytkownik*** &ndash; osoba, która wykorzystuje aplikacje do wyświetlania wyników analizy, w szczególności nie zajmuje się i nie zna się na zmienianiu wyliczanych statystyk i konfigurowaniu zależności między nimi, jest przypisany do organizacji, która zajmuje się tym za niego; może mieć przypisany ograniczony zbiór zmiennych, o których wie, i, które może wyświetlać. 

***Organizacja*** &ndash; podmiot odpowiedzialny za zdefiniowanie i zarządzanie wybranymi zmiennymi; ma przypisanych użytkowników i może definiować, do czego mają dostęp. 


# Mikroserwisy

## Frontend

Nie ma tu żadnego opisu. Każdy wie, co robi frontend.

## Użytkownicy 

Obsługuje bazę danych użytkowników, udostępnia dane wybranego użytkownika i obsługuje logowanie i wylogowywanie. 

## Organizacje

Umożliwia zarządzanie aplikacjami, tj. podgląd zadeklarowanych zmiennych, możliwość wstrzymywania i wznawiania obliczeń. Dodatkowo umożliwia tworzenie użytkowników/podpinanie użytkowników i udostępnianie im zmiennych. 

Ten serwis jest wykorzystywany przez wszystkie serwisy poza tymi, które zostały zaprojektowane jako łatwo wymienialne. Możliwe, że potrzebny jest jakiś redesign, może trzeba wydzielić cześć trzymającą informacje, co należy liczyć, a może trzeba zaakceptować, że ten serwis ma informacje, których wszyscy inni potrzebują.

## Zmienne 

Odpowiada za utrzymanie wiedzy na temat zadeklarowanych zmiennych. Umożliwia dopisywanie nowych informacji i udostępnia już zapisane. Do tego wysyła i odbiera dane od koordynatora obliczeń, aby umożliwić wyliczanie nowych informacji na temat zmiennych. 

## Koordynator obliczeń

Przyjmuje dane potrzebne do wyliczania zmiennych, rozdziela te informacji między nody mikroserwisu obliczenia, zbiera wyniki i zwraca je do mikroserwisu zmienne. Jego zadaniem jest radzenie sobie ze wszystkimi problemami występującymi przy rozproszonych obliczeniach. Musi dbać oto, aby każdy wynik był zwrócony dokładnie raz, aby stan nodów nie został zepsuty, aby obliczenia były w miarę równo rozłożone, ... .

## Obliczenia

Oblicza wartości zmiennych. Powinien być zaprojektowany tak, aby dało się go łatwo wymieniać. Nie powinien samodzielnie zajmować się problemami obliczeń na wielu maszynach, ale będą nałożone pewne ograniczenia na to, co może robić.

Możliwe, że rozsądne jest dodanie wymagania, że ten serwis albo jego nody mają mieć informacje, jakie zmienne wyliczają i jakich danych do tego potrzebują. 

## Scrapper

Odpowiada za pobieranie danych z zewnątrz. Podobnie jak mikroserwis obliczenia, powinien dawać się łatwo wymieniać. 


# Zależności 

### Frontend &ndash; Użytkownicy 
Oczywiste.

### Frontend &ndash; Organizacje
Oczywiste.

### Frontend &ndash; Zmienne
Udostępnia wartości do wyświetlenia. 

### Użytkownicy &ndash; Organizacje
Wymiana informacji na temat tego, do jakich zmiennych dany użytkownik ma dostęp. Chyba organizacja powinna trzymać informacje na temat dostępnych zmiennych, a użytkownicy powinni je tylko pobierać. 

### Zmienne &ndash; Organizacje
Udostępniają informacje na temat tego, co powinno być wyliczane. 

### Zmienne &ndash; Koordynator obliczeń
Wysyła dane wymagane do obliczania nowych wartości zmiennych i otrzymuje nowe dane. 

### Zmienne &ndash; Scrapper
Otrzymuje informacje na temat zmiennych, które później są wykorzystywane do wyliczania innych zmiennych.

### Koordynator obliczeń &ndash; Obliczenia
Zleca każdemu nodowy i serwisie obliczenia, którymi zmiennymi powinien się zajmować oraz wysyła i odbiera odpowiednie dane. 

### Koordynator obliczeń &ndash; Organizacje
Trzeba jakoś ogarnąć wymianę informacji o tym, co i jak wyliczać. 
