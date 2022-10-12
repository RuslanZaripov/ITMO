use crate::ray::Ray;
use crate::Vec3;
use crate::material::Material;

#[derive(Copy, Clone)]
pub struct HitRecord<'a> {
    pub factor: f64,
    pub point: Vec3,
    pub normal: Vec3,
    pub material: &'a (dyn Material + 'a),
    pub front_face: bool,
    pub u: f64,
    pub v: f64,
}

pub trait Hittable {
    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> Option<HitRecord>;
}

pub struct HittableList {
    objects: Vec<Box<dyn Hittable>>
}

impl HittableList {
    pub fn new() -> Self {
        Self { objects: Vec::new() }
    }

    pub fn clear(&mut self) {
        self.objects.clear();
    }

    pub fn add(&mut self, object: impl Hittable + 'static) {
        self.objects.push(Box::new(object));
    }
}

impl Hittable for HittableList {
    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> Option<HitRecord> {
        let mut hit_record = None;
        let mut dist_to_closest = t_max;
        for object in &self.objects {
            if let Some(rec) = object.hit(ray, t_min, dist_to_closest) {
                dist_to_closest = rec.factor;
                hit_record = Some(rec);
            }
        }
        hit_record
    }
}
