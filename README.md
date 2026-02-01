## Opis gry

Rozpoczynasz grę z jedną skolonizowaną planetą w systemie Sol. Twoim celem jest ekspansja w galaktyce poprzez:
- Kolonizację nowych planet
- Budowę infrastruktury i budynków
- Badanie technologii
- Produkcję statków kosmicznych
- Zarządzanie zasobami i populacją

Gra odbywa się w systemie turowym - każda tura reprezentuje pewien okres czasu, w którym wszystkie planety produkują zasoby, populacja rośnie, a floty się przemieszczają.

---

## Instrukcja obsługi

### Podstawowe sterowanie

**Widok galaktyki:**
- **Lewy przycisk myszy + przeciągnięcie** - przesuwanie kamery
- **Scroll myszy** - przybliżanie/oddalanie
- **Kliknięcie na gwiazdę** - wyświetlenie informacji o systemie

**Interfejs główny:**
- **Zakończ turę** - kończy obecną turę i przetwarza wszystkie akcje
- **Badania** - otwiera panel technologii
- **Floty** - wyświetla listę wszystkich flot

### Zarządzanie planetą

Po kliknięciu na planetę w systemie otwiera się panel zarządzania:

1. **Populacja:**
   - Przypisuj obywateli do produkcji żywności (🌾)
   - Przypisuj do produkcji przemysłowej (🏭)
   - Przypisuj do badań (🔬)
   - **Wszyscy obywatele muszą być przypisani przed zakończeniem tury!**

2. **Kolejka budowy:**
   - Dodaj budynki lub statki do kolejki
   - Zmieniaj kolejność przyciskami ↑/↓
   - Usuwaj pozycje przyciskiem X
   - **Rush Buy** - zakup natychmiastowy za kredyty (tylko dla pierwszej pozycji)

3. **Przyciski akcji:**
   - **Dodaj do kolejki** - wybierz budynek lub statek do produkcji
   - **← Powrót do systemu** - wróć do widoku systemu

### Mechanika wzrostu populacji

- Populacja rośnie gdy jest nadwyżka żywności
- Każdy obywatel konsumuje 0.4 żywności/turę
- Niedobór żywności prowadzi do śmierci głodowej
- Limit populacji zależy od typu planety i budynków

### Kolonizacja nowych planet

1. Zbuduj **Statek kolonizacyjny** (wymaga 1 populacji z planety)
2. Przenieś flotę do systemu z nieskolonizowaną planetą nadającą się do zamieszkania
3. Kliknij na planetę i wybierz **Skolonizuj planetę**
4. Statek zostanie zużyty, a planeta skolonizowana z 1 populacją

### System badań

1. Otwórz panel **Badania**
2. Wybierz technologię do badania (sprawdź wymagania)
3. Każda tura dodaje punkty badań z planet i instalacji
4. Po ukończeniu badania odblokowujesz nowe możliwości

### Floty i eksploracja

**Tworzenie floty:**
- Statki produkowane na planetach automatycznie dołączają do floty w systemie
- Możesz rozdzielać floty używając opcji **Oddziel statki**

**Przemieszczanie flot:**
1. Kliknij na system z flotą
2. W panelu floty wybierz **Zarządzaj**
3. Kliknij **Przenieś flotę do wybranego systemu**
4. Wybierz cel - wyświetli się liczba tur i trasa
5. Flota automatycznie przemieszcza się co turę

**Anulowanie ruchu:**
- W zarządzaniu flotą wybierz **Anuluj podróż**

### Instalacje kosmiczne

Posiadając **Fabrykę Kosmiczną** w flocie możesz budować:
- **Laboratorium Asteroidowe** - +5 badań/turę
- **Kopalnia Asteroidowa** - +5 kredytów/turę
- **Kopalnia Gazowa** - +8 kredytów/turę (tylko na gazowych gigantach)
- **Posterunek Bojowy** - obrona systemu (wymaga technologii)

---

## Wykorzystane mechaniki

### System zasobów
- **Kredyty** (💰) - waluta do rush buyów i transakcji
- **Badania** (🔬) - punkty do odblokowywania technologii
- **Produkcja** (🏭) - budowa budynków i statków
- **Żywność** (🌾) - utrzymanie i wzrost populacji

### Typy planet

**Planety nadające się do zamieszkania:**
- **Ziemiopodobna** - najlepsza, max 12 populacji
- **Oceaniczna** - bardzo dobra, max 12 populacji
- **Pustynna** - średnia, max 10 populacji
- **Tundrowa** - średnia, max 10 populacji

**Planety niezamieszkalne:**
- Jałowa, Toksyczna, Radioaktywna, Wulkaniczna, Lodowa
- Można kolonizować po zbadaniu odpowiednich technologii (planowane)

### Atrybuty planet
- **Złoża złota** - +3 💰/turę
- **Starożytne artefakty** - +3 🔬/turę
- **Żyzne glony** - +2 🌾/turę

### Rozmiary planet
- **Mała** - penalty do max populacji
- **Średnia** - standard
- **Duża** - bonus do max populacji
- **Ogromna** - duży bonus do max populacji

### Bogactwo planet
- **Uboga** - -1 do produkcji
- **Normalna** - brak modyfikatora
- **Bogata** - +2 do produkcji
- **Ultra bogata** - +4 do produkcji

### Budynki

**Podstawowe (dostępne od startu):**
- Farma - zwiększa produkcję żywności
- Fabryka - zwiększa produkcję przemysłową
- Laboratorium - zwiększa badania
- Osada Górnicza - produkcja + kredyty
- Centrum Administracyjne - kredyty
- Wieża Komunikacyjna - badania
- Targ Kolonialny - kredyty
- Osiedle Mieszkalne - zwiększa limit populacji

**Zaawansowane (wymagają technologii):**
- Kopalnia Księżycowa - produkcja i kredyty (wymaga księżyca)
- Zaawansowana Farma - znacznie zwiększa żywność
- Centrum Badawcze - duży bonus do badań
- Fabryka Kosmiczna - masywny bonus do produkcji
- Posterunek Bojowy - obrona systemu
- Bank Galaktyczny - duży bonus do kredytów
- Akademia Kolonialna - populacja + badania + kredyty
- Huta Planetarna - duży bonus do produkcji
- Centrum Logistyczne - produkcja + kredyty
- Megamiasto - ogromny bonus do populacji + kredyty

### Statki

**Dostępne od startu:**
- **Zwiadowca** - szybki statek rozpoznawczy
- **Statek kolonizacyjny** - kolonizacja planet

**Bojowe (wymagają technologii):**
- **Myśliwiec** - lekki statek bojowy
- **Niszczyciel** - uniwersalny średni statek
- **Krążownik** - ciężki statek bojowy
- **Pancernik** - potężny okręt liniowy
- **Lotniskowiec** - wsparcie floty

**Specjalne:**
- **Fabryka Kosmiczna** - budowa instalacji w kosmosie

### Drzewo technologii

**Przemysł:**
- Ulepszona produkcja → Konstrukcje kosmiczne
- Ulepszone rolnictwo → Wzrost populacji → Urbanizacja
- Zaawansowane górnictwo

**Badania:**
- Zaawansowane badania

**Militarne:**
- Podstawowe uzbrojenie → Niszczyciele
- Podstawowe uzbrojenie → Ulepszone uzbrojenie → Ciężkie statki → Okręty liniowe
- Podstawowe uzbrojenie → Ulepszone opancerzenie
- Zaawansowana doktryna floty
- Platformy obronne

**Ekonomia:**
- Sieci handlowe → Zaawansowana ekonomia
- Administracja kolonialna

### System ruchu flot
- Floty poruszają się po ścieżkach między połączonymi systemami
- Algorytm BFS znajduje najkrótszą trasę
- Każdy skok = 1 tura
- Wyświetlana jest pełna trasa z czasem przybycia

### Rush Buy
- Możliwość natychmiastowego zakupu za kredyty
- Koszt: pozostała produkcja × 2 kredyty
- Dostępne tylko dla pierwszej pozycji w kolejce

---

## Znane błędy i ograniczenia

### Brak AI przeciwników
Obecnie gra nie posiada przeciwników AI, ponieważ zabrakło czasu:
- Wrogie cywilizacje
- Walki kosmiczne
- Podboje planet

### Brak mgły wojny
- Wszystkie systemy są widoczne od początku
- Planowane: system eksploracji i odkrywania nieznanych sektorów

### Brak systemu walki
- Statki bojowe są produkowane ale nie ma mechaniki walki
- Planowane: system walki turowej lub auto-resolve


