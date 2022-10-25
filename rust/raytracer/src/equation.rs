pub struct QuadraticEquation {
    a: f64,
    b: f64,
    c: f64,
    discriminant: f64,
}

impl std::fmt::Display for QuadraticEquation {
    fn fmt(&self, f: &mut std::fmt::Formatter) -> std::fmt::Result {
        write!(f, "{} * t^2 + {} * t + {} = 0; D = {}",
               self.a, self.b, self.c, self.discriminant
        )
    }
}

impl QuadraticEquation {
    pub fn new(a: f64, b: f64, c: f64) -> QuadraticEquation {
        QuadraticEquation { a, b, c, discriminant: b * b - 4.0 * a * c, }
    }

    pub fn solve(&self) -> Option<(f64, f64)> {
        if self.discriminant < 0.0 {
            return None
        }
        let t1 = (-self.b - self.discriminant.sqrt()) / (2.0 * self.a);
        let t2 = (-self.b + self.discriminant.sqrt()) / (2.0 * self.a);
        Some((t1, t2))
    }

    pub fn get_discriminant(&self) -> f64 {
        self.discriminant
    }
}

pub struct LinearEquation {
    a: f64,
    b: f64,
    x: f64,
}

impl std::fmt::Display for LinearEquation {
    fn fmt(&self, f: &mut std::fmt::Formatter) -> std::fmt::Result {
        write!(f, "{} * t + {} = {}", self.a, self.b, self.x)
    }
}

impl LinearEquation {
    pub fn new(a: f64, b: f64, x: f64) -> LinearEquation {
        LinearEquation { a, b, x }
    }

    pub fn solve(&self) -> Option<f64> {
        if self.a == 0.0 {
            return None
        }
        let denominator = 1.0 / self.a;
        Some((self.x - self.b) * denominator)
    }
}